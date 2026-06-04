package com.example.Zhora.service.impl;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.record.ConversionMultipartFile;
import com.example.Zhora.service.repository.MinioServiceImpl;
import io.minio.StatObjectResponse;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class AbstractConversionServiceImplTest {

    @Mock
    private MinioServiceImpl minioService;

    @Mock
    private StatObjectResponse statObjectResponse;

    private TestConversionServiceImpl conversionService;

    private static class TestConversionServiceImpl extends AbstractConversionServiceImpl {
        private boolean allowed = true;

        public TestConversionServiceImpl(MinioServiceImpl minioService) {
            super(minioService);
        }

        @Override
        public byte[] parsing(InputStream inputStream) {
            return "converted-content".getBytes();
        }

        @Override
        public boolean checkAllowedTypes(String fromExtension, String toExtension) {
            return allowed;
        }

        public void setAllowed(boolean allowed) {
            this.allowed = allowed;
        }
    }

    @BeforeEach
    void setUp() {
        conversionService = new TestConversionServiceImpl(minioService);
    }

    @Test
    void convert_WhenSuccess_ShouldReturnMultipartFile() throws Exception {
        UUID fileId = UUID.randomUUID();
        FileConversionInbox file = new FileConversionInbox();
        file.setId(fileId);
        file.setName("document.docx");
        file.setBucketName("source-bucket");

        ByteArrayInputStream inputStream = new ByteArrayInputStream("content".getBytes());
        when(minioService.downloadObject(file)).thenReturn(inputStream);

        ConversionMultipartFile result = conversionService.convert(file);

        assertThat(result).isNotNull();
        assertThat(result.getFileConversionInboxUuid()).isEqualTo(fileId);
        assertThat(result.getName()).isEqualTo("document.pdf");
        assertThat(result.getOriginalFilename()).isEqualTo("document.docx");
        assertThat(result.getFromBucketName()).isEqualTo("source-bucket");
        assertThat(result.getContent()).isEqualTo("converted-content".getBytes());
        assertThat(result.getContentType()).isEqualTo("document/pdf");
    }

    @Test
    void convert_WhenIOExceptionOccurs_ShouldThrowConversionException() throws Exception {
        FileConversionInbox file = new FileConversionInbox();
        file.setName("document.docx");
        file.setBucketName("source-bucket");

        when(minioService.downloadObject(file)).thenThrow(new ConversionException("Minio error"));

        assertThatThrownBy(() -> conversionService.convert(file))
                .isInstanceOf(ConversionException.class);
    }

    @Test
    void checkConversion_WhenAllowed_ShouldReturnTrue() throws Exception {
        FileConversionInbox file = new FileConversionInbox();
        file.setToExtension("pdf");

        when(minioService.statObject(file)).thenReturn(statObjectResponse);
        when(statObjectResponse.object()).thenReturn(null);
        when(statObjectResponse.object()).thenReturn("document.docx");
        conversionService.setAllowed(true);

        boolean result = conversionService.checkConversion(file);

        assertThat(result).isTrue();
    }

    @Test
    void checkConversion_WhenNotAllowed_ShouldReturnFalse() throws Exception {
        FileConversionInbox file = new FileConversionInbox();
        file.setToExtension("exe");

        when(minioService.statObject(file)).thenReturn(statObjectResponse);
        when(statObjectResponse.object()).thenReturn("document.docx");
        conversionService.setAllowed(false);

        boolean result = conversionService.checkConversion(file);

        assertThat(result).isFalse();
    }
}
