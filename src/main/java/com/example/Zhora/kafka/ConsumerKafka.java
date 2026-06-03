package com.example.Zhora.kafka;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.entity.mapper.FileConversionInboxMapper;
import com.example.Zhora.record.ConversionRequestRecord;
import com.example.Zhora.service.repository.FileConversionInboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.springframework.kafka.annotation.KafkaListener;
import org.springframework.stereotype.Component;

import java.util.Objects;
import java.util.UUID;


@Slf4j
@Component
@RequiredArgsConstructor
public class ConsumerKafka {

    private final FileConversionInboxService fileConversionService;
    private final FileConversionInboxMapper fileConversionMapper;
    private final ObjectMapper objectMapper;

    @KafkaListener(groupId = "conversion", topics = "conversion-request")
    public void handleConversionFile(ConsumerRecord<String, String> record) {
        String recordStr = record.value();
        log.debug("Received ConversionRequestRecord: {}", recordStr);

        try {
            ConversionRequestRecord value = objectMapper.readValue(recordStr, ConversionRequestRecord.class);

            UUID fileId = fileConversionService.findUuidByBucketNameAndName(
                    value.bucketName(),
                    value.fileName(),
                    value.toExtension()
            );

            if (Objects.isNull(fileId)) {
                FileConversionInbox fileConversion = fileConversionMapper.toFileConversion(value);
                fileConversionService.save(fileConversion);
                log.debug("Successfully saved file conversion with id: {}", fileConversion.getId());
            } else {
                log.debug("File already exists with id: {}", fileId);
            }
        } catch (JsonProcessingException e) {
            log.debug("Error conversing record: {}", recordStr, e);
        }
    }
}
