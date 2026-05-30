package com.example.Zhora.kafka;

import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ProducerKafka {

    private final KafkaTemplate<String, Object> kafkaTemplate;


    public void sendMessage(String topic, String message) {
        kafkaTemplate.send(topic, message);
        log.info("Send message {} to kafka topic {}", message, topic);
    }
}
