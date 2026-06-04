package com.example.Zhora.kafka;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.entity.mapper.FileConversionInboxMapper;
import com.example.Zhora.record.ConversionRequestRecord;
import com.example.Zhora.service.repository.FileConversionInboxService;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.clients.consumer.ConsumerRecord;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.UUID;

import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConsumerKafkaTest {

    @Mock
    private FileConversionInboxService fileConversionService;

    @Mock
    private FileConversionInboxMapper fileConversionMapper;

    @Mock
    private ObjectMapper objectMapper;

    @InjectMocks
    private ConsumerKafka consumerKafka;

    private ConsumerRecord<String, String> consumerRecord;
    private ConversionRequestRecord requestRecord;
    private String jsonPayload;

    @BeforeEach
    void setUp() {
        jsonPayload = "{\"bucketName\":\"test-bucket\",\"fileName\":\"doc.txt\",\"toExtension\":\"pdf\"}";
        consumerRecord = new ConsumerRecord<>("conversion-request", 0, 0L, "key", jsonPayload);
        requestRecord = new ConversionRequestRecord("test-bucket", "doc.txt", "pdf");
    }

    @Test
    void handleConversionFile_WhenFileDoesNotExist_ShouldSaveRecord() throws JsonProcessingException {
        when(objectMapper.readValue(jsonPayload, ConversionRequestRecord.class)).thenReturn(requestRecord);
        when(fileConversionService.findUuidByBucketNameAndName("test-bucket", "doc.txt", "pdf")).thenReturn(null);

        FileConversionInbox mockEntity = new FileConversionInbox();
        when(fileConversionMapper.toFileConversion(requestRecord)).thenReturn(mockEntity);

        consumerKafka.handleConversionFile(consumerRecord);

        verify(fileConversionService, times(1)).save(mockEntity);
    }

    @Test
    void handleConversionFile_WhenFileAlreadyExists_ShouldNotSaveRecord() throws JsonProcessingException {
        UUID existingUuid = UUID.randomUUID();
        when(objectMapper.readValue(jsonPayload, ConversionRequestRecord.class)).thenReturn(requestRecord);
        when(fileConversionService.findUuidByBucketNameAndName("test-bucket", "doc.txt", "pdf")).thenReturn(existingUuid);

        consumerKafka.handleConversionFile(consumerRecord);

        verify(fileConversionMapper, never()).toFileConversion(any());
        verify(fileConversionService, never()).save(any());
    }

    @Test
    void handleConversionFile_WhenJsonExceptionOccurs_ShouldCatchExceptionAndLog() throws JsonProcessingException {
        when(objectMapper.readValue(jsonPayload, ConversionRequestRecord.class))
                .thenThrow(new JsonProcessingException("Invalid JSON") {});

        consumerKafka.handleConversionFile(consumerRecord);

        verify(fileConversionService, never()).findUuidByBucketNameAndName(any(), any(), any());
        verify(fileConversionService, never()).save(any());
    }
}
