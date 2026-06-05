package com.example.Zhora.service.scheduler;

import com.example.Zhora.entity.FileConversionOutbox;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.kafka.ProducerKafka;
import com.example.Zhora.record.ConversionMultipartFile;
import com.example.Zhora.service.impl.workflow.WorkflowConversionServiceImpl;
import com.example.Zhora.service.repository.FileConversionOutboxService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionScheduler {

    private final WorkflowConversionServiceImpl workflowConvertService;
    private final FileConversionOutboxService fileConversionOutboxService;
    private final ProducerKafka producerKafka;

    @Value("${server.conversion.limit}")
    private int LIMIT;

    @Scheduled(fixedRateString = "${server.scheduler.fixedRateRead}")
    public void scheduleConversion() {
        log.debug("Starting scheduler conversion");

        try {
            List<Future<ConversionMultipartFile>> futures = workflowConvertService.workflowConvert();
            log.debug("Finished scheduler conversion files: {}", futures.size());

            if(!futures.isEmpty()){
                log.debug("Starting workflowSave files: {}", futures.size());

                for (Future<ConversionMultipartFile> future : futures) {
                    try {
                        ConversionMultipartFile file = future.get();
                        workflowConvertService.save(file);
                    } catch (ExecutionException | ConversionException e) {
                        log.debug("Error conversion file: {}", e.getMessage(), e);
                    }
                }
                log.debug("Finished workflowSave files");
            }
        } catch (InterruptedException e) {
            log.debug("Error save: {}", e.getMessage(), e);
        }

        log.debug("Finished scheduler conversion");
    }

    @Scheduled(fixedRateString = "${server.scheduler.fixedRateWrite}")
    public void scheduledResponse(){
        List<FileConversionOutbox> files = fileConversionOutboxService.findAll(LIMIT);
        if(!files.isEmpty()){
            log.debug("Starting scheduler response");
            producerKafka.sendMessage(files);
            for(FileConversionOutbox file : files){
                file.setSent(true);
            }
            fileConversionOutboxService.save(files);
            log.debug("Finished scheduler response");
        }
    }
}