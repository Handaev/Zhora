package com.example.Zhora.service.impl.workflow;


import com.example.Zhora.entity.FileConversion;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.service.WorkflowConversionService;
import com.example.Zhora.service.impl.ChangeConversionServiceImpl;
import com.example.Zhora.service.repository.MinioServiceImpl;
import com.example.Zhora.service.repository.FileConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.*;

@Service
@RequiredArgsConstructor
public class WorkflowConversionServiceImpl implements WorkflowConversionService {

    private final FileConversionService fileConversionService;
    private final ChangeConversionServiceImpl changeConversionService;
    private final MinioServiceImpl minioService;

    @Value("${server.conversion.limit}")
    private int LIMIT;

    @Value("${server.conversion.bucketForPdf}")
    private String bucketForPdf;

    public List<Future<MultipartFile>> workflowConvert() {
        List<FileConversion> filesNeedConversion = fileConversionService.findFilesNoConversion(LIMIT);

        if(filesNeedConversion.isEmpty()){
            return List.of();
        }

        List<Future<MultipartFile>> futures =  new ArrayList<>();
        List<Callable<MultipartFile>> tasksConversion =  new ArrayList<>();

        fillTasksConversion(filesNeedConversion, tasksConversion, futures);

        ExecutorService executorService = Executors.newFixedThreadPool(tasksConversion.size());

        try {
            futures.addAll(executorService.invokeAll(tasksConversion));
        } catch (InterruptedException e) {
            //TODO разобраться как правильно его обрабатывать
        }

        return futures;
    }

    private void fillTasksConversion(List<FileConversion> filesNeedConversion,
                                     List<Callable<MultipartFile>> tasksConversion,
                                     List<Future<MultipartFile>> futures) {
        for (FileConversion fileConversion : filesNeedConversion) {
            try {
                tasksConversion.add(
                        () -> changeConversionService.changeConversion(fileConversion).convertFile(fileConversion)
                );
            } catch (ConversionException e) {
                //TODO нужно обработать чтобы не потерять объекты которые нельзя конвертировать добавлять в futures или как то еще и много исключений в convertFile
            }
        }
    }

    @Override
    @Transactional
    public void workflowSave(List<Future<MultipartFile>> files) throws ExecutionException, InterruptedException, IOException {
        for (Future<MultipartFile> future : files) {
            MultipartFile file = future.get();
            minioService.putObject(file, bucketForPdf);
        }
    }
}
