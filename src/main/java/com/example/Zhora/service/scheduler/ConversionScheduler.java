package com.example.Zhora.service.scheduler;

import com.example.Zhora.service.impl.workflow.WorkflowConversionServiceImpl;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.Future;

@Slf4j
@Service
@RequiredArgsConstructor
public class ConversionScheduler {

    private final WorkflowConversionServiceImpl workflowConvertService;

    @Scheduled(fixedRateString = "${server.scheduler.fixedRate}")
    public void scheduleConversion() {
        log.debug("Starting scheduler conversion");

        List<Future<MultipartFile>> futures = workflowConvertService.workflowConvert();
        log.debug("Finished scheduler conversion files: {}", futures.size());


        if(!futures.isEmpty()){
            log.debug("Starting scheduler save files: {}", futures.size());
            workflowConvertService.workflowSave(futures);
        }

        log.debug("Finished scheduler conversion");
    }
}
