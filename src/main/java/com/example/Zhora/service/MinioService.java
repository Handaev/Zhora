package com.example.Zhora.service;


import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Objects;
import java.util.function.Function;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioService {

    @Value("${minio.bucket.name}")
    private String BUCKET_NAME;

    private final MinioClient minioClient;

    public InputStream downloadFile(String fileName) throws MinioException, IOException {
        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(fileName)
                        .build())) {
            if (Objects.isNull(inputStream)) {
                throw new NoSuchFileException(String.format("Отсутствует файл с названием: %s", fileName));
            }

            return inputStream;
        } catch (MinioException e) {
            throw new MinioException(e);
        } catch (IOException e) {
            throw new IOException(e);
        }
    }

    public StatObjectResponse getMetadataFile(String fileName) throws MinioException, NoSuchFileException {
        StatObjectResponse statObjectResponse = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(BUCKET_NAME)
                        .object(fileName)
                        .build()
        );
        if (Objects.isNull(statObjectResponse)) {
            throw new NoSuchFileException(String.format("Отсутствует файл с названием: %s", fileName));
        }

        return statObjectResponse;
    }


}