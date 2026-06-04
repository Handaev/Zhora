package com.example.Zhora.kafka;

import com.example.Zhora.entity.FileConversionOutbox;
import com.example.Zhora.entity.mapper.FileConversionOutboxMapper;
import com.example.Zhora.record.ConversionResponseRecord;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ProducerKafkaTest {

    @Mock
    private KafkaTemplate<String, Object> kafkaTemplate;

    @Mock
    private FileConversionOutboxMapper fileConversionOutboxMapper;

    @InjectMocks
    private ProducerKafka producerKafka;

    private final String testTopic = "test-conversion-response-topic";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(producerKafka, "TOPIC", testTopic);
    }

    @Test
    void sendMessage_WhenListIsNotEmpty_ShouldMapAndSendToKafka() {
        FileConversionOutbox mockOutbox = new FileConversionOutbox();
        List<FileConversionOutbox> records = List.of(mockOutbox);
        ConversionResponseRecord mockResponseRecord = new ConversionResponseRecord("test-bucket", "result.pdf", "SUCCESS");

        when(fileConversionOutboxMapper.toConversionResponseRecord(mockOutbox)).thenReturn(mockResponseRecord);

        ArgumentCaptor<ProducerRecord<String, Object>> recordCaptor = ArgumentCaptor.forClass(ProducerRecord.class);

        producerKafka.sendMessage(records);

        verify(fileConversionOutboxMapper, times(1)).toConversionResponseRecord(mockOutbox);
        verify(kafkaTemplate, times(1)).send(recordCaptor.capture());

        ProducerRecord<String, Object> sentRecord = recordCaptor.getValue();
        assertThat(sentRecord.topic()).isEqualTo(testTopic);
        assertThat(sentRecord.value()).isEqualTo(mockResponseRecord);
    }

    @Test
    void sendMessage_WhenListIsEmpty_ShouldNotSendAnything() {
        List<FileConversionOutbox> emptyRecords = Collections.emptyList();

        producerKafka.sendMessage(emptyRecords);

        verifyNoInteractions(fileConversionOutboxMapper);
        verifyNoInteractions(kafkaTemplate);
    }
}
