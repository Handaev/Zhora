package com.example.Zhora.service.repository;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.exception.ConversionException;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.ObjectWriteResponse;
import io.minio.GetObjectResponse;
import io.minio.PutObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.StatObjectArgs;
import io.minio.errors.MinioException;
import okhttp3.Headers;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class MinioServiceImplTest {

    @Mock
    private MinioClient minioClient;

    @InjectMocks
    private MinioServiceImpl minioService;

    @Test
    void downloadObject_WhenSuccess_ShouldReturnGetObjectResponse() throws Exception {
        FileConversionInbox file = new FileConversionInbox();
        file.setName("test.txt");
        file.setBucketName("bucket");

        InputStream contentStream = new ByteArrayInputStream("data".getBytes());
        Headers headers = new Headers.Builder().build();
        GetObjectResponse mockResponse = new GetObjectResponse(headers, "bucket", null, "test.txt", contentStream);

        when(minioClient.getObject(any(GetObjectArgs.class))).thenReturn(mockResponse);

        InputStream result = minioService.downloadObject(file);

        assertThat(result).isNotNull().isInstanceOf(GetObjectResponse.class);
        verify(minioClient, times(1)).getObject(any(GetObjectArgs.class));
    }

    @Test
    void downloadObject_WhenMinioExceptionOccurs_ShouldThrowConversionException() throws Exception {
        FileConversionInbox file = new FileConversionInbox();
        file.setName("test.txt");
        file.setBucketName("bucket");

        when(minioClient.getObject(any(GetObjectArgs.class))).thenThrow(new MinioException("Minio error") {});

        assertThatThrownBy(() -> minioService.downloadObject(file))
                .isInstanceOf(ConversionException.class)
                .hasMessageContaining("Ошибка при получении файла test.txt");
    }

    @Test
    void statObject_WhenSuccess_ShouldReturnResponse() throws Exception {
        FileConversionInbox file = new FileConversionInbox();
        file.setName("test.txt");
        file.setBucketName("bucket");

        StatObjectResponse mockResponse = mock(StatObjectResponse.class);
        when(minioClient.statObject(any(StatObjectArgs.class))).thenReturn(mockResponse);

        StatObjectResponse result = minioService.statObject(file);

        assertThat(result).isSameAs(mockResponse).isEqualTo(mockResponse);
    }

    @Test
    void statObject_WhenMinioExceptionOccurs_ShouldThrowConversionException() throws Exception {
        FileConversionInbox file = new FileConversionInbox();
        file.setName("test.txt");
        file.setBucketName("bucket");

        when(minioClient.statObject(any(StatObjectArgs.class))).thenThrow(new MinioException("Minio error") {});

        assertThatThrownBy(() -> minioService.statObject(file))
                .isInstanceOf(ConversionException.class)
                .hasMessageContaining("Ошибка при получении файла test.txt");
    }

    @Test
    void putObject_WhenSuccess_ShouldReturnResponse() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getName()).thenReturn("file.pdf");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        when(mockFile.getSize()).thenReturn(4L);
        when(mockFile.getContentType()).thenReturn("application/pdf");

        ObjectWriteResponse mockResponse = mock(ObjectWriteResponse.class);
        when(minioClient.putObject(any(PutObjectArgs.class))).thenReturn(mockResponse);

        ObjectWriteResponse result = minioService.putObject(mockFile, "target-bucket");

        assertThat(result).isSameAs(mockResponse);
        verify(minioClient, times(1)).putObject(any(PutObjectArgs.class));
    }

    @Test
    void putObject_WhenMinioExceptionOccurs_ShouldThrowConversionException() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getName()).thenReturn("file.pdf");
        when(mockFile.getInputStream()).thenReturn(new ByteArrayInputStream("data".getBytes()));
        when(mockFile.getSize()).thenReturn(4L);
        when(mockFile.getContentType()).thenReturn("application/pdf");
        when(minioClient.putObject(any(PutObjectArgs.class))).thenThrow(new MinioException("Minio error"));

        assertThatThrownBy(() -> minioService.putObject(mockFile, "target-bucket"))
                .isInstanceOf(ConversionException.class)
                .hasMessageContaining("Ошибка при обновлении файла file.pdf");
    }

    @Test
    void putObject_WhenIOExceptionOccurs_ShouldThrowConversionException() throws Exception {
        MultipartFile mockFile = mock(MultipartFile.class);
        when(mockFile.getName()).thenReturn("file.pdf");
        when(mockFile.getInputStream()).thenThrow(new IOException("Stream error"));

        assertThatThrownBy(() -> minioService.putObject(mockFile, "target-bucket"))
                .isInstanceOf(ConversionException.class)
                .hasMessageContaining("Ошибка при получении потока file.pdf");
    }
}
