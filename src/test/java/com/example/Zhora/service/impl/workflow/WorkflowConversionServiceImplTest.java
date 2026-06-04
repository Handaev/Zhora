package com.example.Zhora.service.impl.workflow;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.entity.FileConversionOutbox;
import com.example.Zhora.record.ConversionMultipartFile;
import com.example.Zhora.service.ConversionService;
import com.example.Zhora.service.impl.ChangeConversionServiceImpl;
import com.example.Zhora.service.repository.FileConversionInboxService;
import com.example.Zhora.service.repository.FileConversionOutboxService;
import com.example.Zhora.service.repository.MinioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.Collections;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Future;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class WorkflowConversionServiceImplTest {

    @Mock
    private FileConversionInboxService fileConversionService;

    @Mock
    private ChangeConversionServiceImpl changeConversionService;

    @Mock
    private MinioServiceImpl minioService;

    @Mock
    private FileConversionOutboxService fileConversionOutboxService;

    @InjectMocks
    private WorkflowConversionServiceImpl workflowConversionService;

    private final int testLimit = 5;
    private final String testBucket = "pdf-bucket";

    @BeforeEach
    void setUp() {
        ReflectionTestUtils.setField(workflowConversionService, "LIMIT", testLimit);
        ReflectionTestUtils.setField(workflowConversionService, "bucketForPdf", testBucket);
    }

    @Test
    void workflowConvert_WhenNoFiles_ShouldReturnEmptyList() throws InterruptedException {
        when(fileConversionService.findFilesNoConversion(testLimit)).thenReturn(Collections.emptyList());

        List<Future<ConversionMultipartFile>> result = workflowConversionService.workflowConvert();

        assertThat(result).isEmpty();
        verifyNoInteractions(changeConversionService);
    }

    @Test
    void workflowConvert_WhenFilesExist_ShouldInvokeTasks() throws InterruptedException {
        FileConversionInbox file1 = new FileConversionInbox();
        List<FileConversionInbox> files = List.of(file1);

        ConversionService mockStrategy = mock(ConversionService.class);

        when(fileConversionService.findFilesNoConversion(testLimit)).thenReturn(files);
        when(changeConversionService.changeConversion(file1)).thenReturn(mockStrategy);

        List<Future<ConversionMultipartFile>> result = workflowConversionService.workflowConvert();

        assertThat(result).hasSize(1);
        verify(fileConversionService, times(1)).findFilesNoConversion(testLimit);
    }

    @Test
    void save_WhenOutboxRecordDoesNotExist_ShouldPutToMinioAndSaveOutboxAndMarkInbox() {
        UUID inboxId = UUID.randomUUID();
        ConversionMultipartFile mockFile = mock(ConversionMultipartFile.class);
        FileConversionInbox mockInbox = new FileConversionInbox();

        when(mockFile.getName()).thenReturn("test.pdf");
        when(mockFile.getFileConversionInboxUuid()).thenReturn(inboxId);
        when(fileConversionOutboxService.findUuidByNameAndBucketName("test.pdf", testBucket)).thenReturn(null);
        when(fileConversionService.findById(inboxId)).thenReturn(mockInbox);

        workflowConversionService.save(mockFile);

        verify(minioService, times(1)).putObject(mockFile, testBucket);
        verify(fileConversionOutboxService, times(1)).save(any(FileConversionOutbox.class));
        verify(fileConversionService, times(1)).findById(inboxId);
        assertThat(mockInbox.isConversion()).isTrue();
    }

    @Test
    void save_WhenOutboxRecordAlreadyExists_ShouldPutToMinioAndOnlyMarkInbox() {
        UUID inboxId = UUID.randomUUID();
        UUID outboxId = UUID.randomUUID();
        ConversionMultipartFile mockFile = mock(ConversionMultipartFile.class);
        FileConversionInbox mockInbox = new FileConversionInbox();

        when(mockFile.getName()).thenReturn("test.pdf");
        when(mockFile.getFileConversionInboxUuid()).thenReturn(inboxId);
        when(fileConversionOutboxService.findUuidByNameAndBucketName("test.pdf", testBucket)).thenReturn(outboxId);
        when(fileConversionService.findById(inboxId)).thenReturn(mockInbox);

        workflowConversionService.save(mockFile);

        verify(minioService, times(1)).putObject(mockFile, testBucket);
        verify(fileConversionOutboxService, never()).save(any(FileConversionOutbox.class));
        verify(fileConversionService, times(1)).findById(inboxId);
        assertThat(mockInbox.isConversion()).isTrue();
    }
}
