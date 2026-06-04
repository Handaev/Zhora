package com.example.Zhora.service.repository;

import com.example.Zhora.entity.FileConversionOutbox;
import com.example.Zhora.repository.FileConversionOutboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class FileConversionOutboxServiceTest {

    @Mock
    private FileConversionOutboxRepository fileConversionOutboxRepository;

    @InjectMocks
    private FileConversionOutboxService fileConversionOutboxService;

    @Test
    void findUuidByNameAndBucketName_WhenExists_ShouldReturnUuid() {
        UUID mockUuid = UUID.randomUUID();
        when(fileConversionOutboxRepository.findUuidByNameAndBucketName("file.pdf", "bucket"))
                .thenReturn(Optional.of(mockUuid));

        UUID result = fileConversionOutboxService.findUuidByNameAndBucketName("file.pdf", "bucket");

        assertThat(result).isEqualTo(mockUuid);
    }

    @Test
    void findUuidByNameAndBucketName_WhenDoesNotExist_ShouldReturnNull() {
        when(fileConversionOutboxRepository.findUuidByNameAndBucketName("file.pdf", "bucket"))
                .thenReturn(Optional.empty());

        UUID result = fileConversionOutboxService.findUuidByNameAndBucketName("file.pdf", "bucket");

        assertThat(result).isNull();
    }

    @Test
    void saveSingle_ShouldReturnSavedEntity() {
        FileConversionOutbox outbox = new FileConversionOutbox();
        when(fileConversionOutboxRepository.save(outbox)).thenReturn(outbox);

        FileConversionOutbox result = fileConversionOutboxService.save(outbox);

        assertThat(result).isSameAs(outbox);
        verify(fileConversionOutboxRepository, times(1)).save(outbox);
    }

    @Test
    void saveList_ShouldInvokeSaveAll() {
        List<FileConversionOutbox> list = List.of(new FileConversionOutbox());

        fileConversionOutboxService.save(list);

        verify(fileConversionOutboxRepository, times(1)).saveAll(list);
    }

    @Test
    void findAll_ShouldReturnList() {
        List<FileConversionOutbox> mockList = List.of(new FileConversionOutbox());
        when(fileConversionOutboxRepository.findAll(10)).thenReturn(mockList);

        List<FileConversionOutbox> result = fileConversionOutboxService.findAll(10);

        assertThat(result).hasSize(1).isSameAs(mockList);
    }
}
