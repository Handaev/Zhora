package com.example.Zhora.service.scheduler;

import com.example.Zhora.entity.FileConversionOutbox;
import com.example.Zhora.kafka.ProducerKafka;
import com.example.Zhora.record.ConversionMultipartFile;
import com.example.Zhora.service.impl.workflow.WorkflowConversionServiceImpl;
import com.example.Zhora.service.repository.FileConversionOutboxService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ConversionSchedulerTest {

    @Mock
    private WorkflowConversionServiceImpl workflowConvertService;

    @Mock
    private FileConversionOutboxService fileConversionOutboxService;

    @Mock
    private ProducerKafka producerKafka;

    @InjectMocks
    private ConversionScheduler conversionScheduler;

    private final int testLimit = 10;

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(conversionScheduler, "LIMIT", testLimit);
    }

    @Test
    void scheduleConversion_WhenNoFiles_ShouldNotSaveAnything() throws InterruptedException {
        when(workflowConvertService.workflowConvert()).thenReturn(Collections.emptyList());

        conversionScheduler.scheduleConversion();

        verify(workflowConvertService, times(1)).workflowConvert();
        verify(workflowConvertService, never()).save(any());
    }

    @Test
    void scheduleConversion_WhenSuccess_ShouldSaveFiles() throws Exception {
        ConversionMultipartFile mockFile = mock(ConversionMultipartFile.class);
        Future<ConversionMultipartFile> mockFuture = mock(Future.class);

        when(mockFuture.get()).thenReturn(mockFile);
        when(workflowConvertService.workflowConvert()).thenReturn(List.of(mockFuture));

        conversionScheduler.scheduleConversion();

        verify(workflowConvertService, times(1)).workflowConvert();
        verify(workflowConvertService, times(1)).save(mockFile);
    }

    @Test
    void scheduleConversion_WhenExecutionException_ShouldCatchAndContinue() throws Exception {
        Future<ConversionMultipartFile> mockFuture = mock(Future.class);

        when(mockFuture.get()).thenThrow(new ExecutionException("Conversion fail", new RuntimeException()));
        when(workflowConvertService.workflowConvert()).thenReturn(List.of(mockFuture));

        conversionScheduler.scheduleConversion();

        verify(workflowConvertService, times(1)).workflowConvert();
        verify(workflowConvertService, never()).save(any());
    }

    @Test
    void scheduledResponse_WhenNoOutboxFiles_ShouldDoNothing() {
        when(fileConversionOutboxService.findAll(testLimit)).thenReturn(Collections.emptyList());

        conversionScheduler.scheduledResponse();

        verify(fileConversionOutboxService, times(1)).findAll(testLimit);
        verifyNoInteractions(producerKafka);
        verify(fileConversionOutboxService, never()).save(anyList());
    }

    @Test
    void scheduledResponse_WhenFilesExist_ShouldSendToKafkaAndMarkAsSent() {
        FileConversionOutbox file1 = new FileConversionOutbox();
        file1.setSent(false);
        List<FileConversionOutbox> files = List.of(file1);

        when(fileConversionOutboxService.findAll(testLimit)).thenReturn(files);

        conversionScheduler.scheduledResponse();

        verify(fileConversionOutboxService, times(1)).findAll(testLimit);
        verify(producerKafka, times(1)).sendMessage(files);
        verify(fileConversionOutboxService, times(1)).save(files);
        assertThat(file1.isSent()).isTrue();
    }
}
