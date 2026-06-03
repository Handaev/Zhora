package com.example.Zhora.service.impl.workflow;


import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.entity.FileConversionOutbox;
import com.example.Zhora.record.ConversionMultipartFile;
import com.example.Zhora.service.WorkflowConversionService;
import com.example.Zhora.service.impl.ChangeConversionServiceImpl;
import com.example.Zhora.service.repository.FileConversionInboxService;
import com.example.Zhora.service.repository.FileConversionOutboxService;
import com.example.Zhora.service.repository.MinioServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.*;

@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowConversionServiceImpl implements WorkflowConversionService {

    private final FileConversionInboxService fileConversionService;
    private final ChangeConversionServiceImpl changeConversionService;
    private final MinioServiceImpl minioService;
    private final FileConversionOutboxService fileConversionOutboxService;

    @Value("${server.conversion.limit}")
    private int LIMIT;

    @Value("${server.conversion.bucketForPdf}")
    private String bucketForPdf;

    public List<Future<ConversionMultipartFile>> workflowConvert() throws InterruptedException {
        List<FileConversionInbox> filesNeedConversion = fileConversionService.findFilesNoConversion(LIMIT);

        if(filesNeedConversion.isEmpty()){
            return List.of();
        }

        List<Callable<ConversionMultipartFile>> tasksConversion =  new ArrayList<>();

        fillTasksConversion(filesNeedConversion, tasksConversion);

        ExecutorService executorService = Executors.newFixedThreadPool(tasksConversion.size());

        return executorService.invokeAll(tasksConversion);
    }

    private void fillTasksConversion(List<FileConversionInbox> filesNeedConversion,
                                     List<Callable<ConversionMultipartFile>> tasksConversion) {
        for (FileConversionInbox fileConversion : filesNeedConversion) {
            tasksConversion.add(
                    () -> changeConversionService.changeConversion(fileConversion).convert(fileConversion)
            );
        }
    }

    @Override
    @Transactional
    public void save(ConversionMultipartFile file) {
        String fileName = file.getName();
        minioService.putObject(file, bucketForPdf);

        UUID fileId = fileConversionOutboxService.findUuidByNameAndBucketName(fileName, bucketForPdf);

        if (fileId == null) {
            fileConversionOutboxService.save(FileConversionOutbox.builder()
                    .name(fileName)
                    .bucketName(bucketForPdf)
                    .build()
            );
        }
        FileConversionInbox inboxFile = fileConversionService.findById(file.getFileConversionInboxUuid());
        inboxFile.setConversion(true);
    }
}
