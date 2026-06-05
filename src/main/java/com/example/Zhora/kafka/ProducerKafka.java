package com.example.Zhora.kafka;

import com.example.Zhora.entity.FileConversionOutbox;
import com.example.Zhora.entity.mapper.FileConversionOutboxMapper;
import com.example.Zhora.record.ConversionResponseRecord;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import lombok.extern.slf4j.Slf4j;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProducerKafka {

    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final FileConversionOutboxMapper fileConversionOutboxMapper;

    @Value("${spring.kafka.producer.topicResponse}")
    private String TOPIC_RESPONSE;

    public void sendMessage(List<FileConversionOutbox> records) {
        for (FileConversionOutbox file : records) {
            ConversionResponseRecord fileConversionOutbox = fileConversionOutboxMapper.toConversionResponseRecord(file);
            kafkaTemplate.send(new ProducerRecord<>(TOPIC_RESPONSE, fileConversionOutbox));
            log.info("Send message to kafka topic count: {}", fileConversionOutbox);
        }
    }
}
