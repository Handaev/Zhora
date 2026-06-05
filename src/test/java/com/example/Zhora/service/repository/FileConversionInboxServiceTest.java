package com.example.Zhora.service.repository;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.repository.FileConversionInboxRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class FileConversionInboxServiceTest {

    @Mock
    private FileConversionInboxRepository fileConversionInboxRepository;

    @InjectMocks
    private FileConversionInboxService fileConversionInboxService;

    @Test
    void findUuidByBucketNameAndName_WhenExists_ShouldReturnUuid() {
        UUID mockUuid = UUID.randomUUID();
        when(fileConversionInboxRepository.findUuidByBucketNameAndNameAndToExtension("bucket", "file.txt", "pdf"))
                .thenReturn(Optional.of(mockUuid));

        UUID result = fileConversionInboxService.findUuidByBucketNameAndName("bucket", "file.txt", "pdf");

        assertThat(result).isEqualTo(mockUuid);
    }

    @Test
    void findUuidByBucketNameAndName_WhenDoesNotExist_ShouldReturnNull() {
        when(fileConversionInboxRepository.findUuidByBucketNameAndNameAndToExtension("bucket", "file.txt", "pdf"))
                .thenReturn(Optional.empty());

        UUID result = fileConversionInboxService.findUuidByBucketNameAndName("bucket", "file.txt", "pdf");

        assertThat(result).isNull();
    }

    @Test
    void save_ShouldReturnSavedEntity() {
        FileConversionInbox file = new FileConversionInbox();
        when(fileConversionInboxRepository.save(file)).thenReturn(file);

        FileConversionInbox result = fileConversionInboxService.save(file);

        assertThat(result).isSameAs(file);
        verify(fileConversionInboxRepository, times(1)).save(file);
    }

    @Test
    void findFilesNoConversion_ShouldReturnList() {
        List<FileConversionInbox> mockList = List.of(new FileConversionInbox());
        when(fileConversionInboxRepository.findFilesNoConversion(5)).thenReturn(mockList);

        List<FileConversionInbox> result = fileConversionInboxService.findFilesNoConversion(5);

        assertThat(result).hasSize(1);
    }

    @Test
    void findById_WhenExists_ShouldReturnEntity() {
        UUID id = UUID.randomUUID();
        FileConversionInbox file = new FileConversionInbox();
        when(fileConversionInboxRepository.findById(id)).thenReturn(Optional.of(file));

        FileConversionInbox result = fileConversionInboxService.findById(id);

        assertThat(result).isSameAs(file);
    }

    @Test
    void findById_WhenDoesNotExist_ShouldReturnNull() {
        UUID id = UUID.randomUUID();
        when(fileConversionInboxRepository.findById(id)).thenReturn(Optional.empty());

        assertThatThrownBy(() -> fileConversionInboxService.findById(id))
                .isInstanceOf(ConversionException.class)
                .hasMessageContaining("Not found file with id: " + id);
    }
}
