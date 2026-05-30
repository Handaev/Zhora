package com.example.Zhora.service.impl;

import com.example.Zhora.entity.FileConversion;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.service.MinioService;
import io.minio.*;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.Objects;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Override
    public InputStream downloadFile(FileConversion fileConversion) throws IOException {
        String fileName = fileConversion.getName();

        try (InputStream inputStream = minioClient.getObject(
                GetObjectArgs.builder()
                        .bucket(fileConversion.getBucketName())
                        .object(fileName)
                        .build())) {
            if (Objects.isNull(inputStream)) {
                throw new NoSuchFileException(String.format("Отсутствует файл с названием: %s", fileName));
            }

            return inputStream;
        } catch (MinioException e) {
            throw new ConversionException(String.format("Ошибка при получении файла %s", fileName), e);
        }
    }

    public StatObjectResponse statObject(FileConversion fileConversion) throws NoSuchFileException {
        String fileName = fileConversion.getName();

        try {
            StatObjectResponse statObjectResponse = minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(fileConversion.getBucketName())
                            .object(fileName)
                            .build()
            );
            if (Objects.isNull(statObjectResponse)) {
                throw new NoSuchFileException(String.format("Отсутствует файл с названием: %s", fileName));
            }

            return statObjectResponse;
        }
        catch (MinioException e) {
            throw new ConversionException(String.format("Ошибка при получении файла %s", fileName), e);
        }
    }

    @Override
    public ObjectWriteResponse putObject(MultipartFile file, String bucketName) throws MinioException, IOException {
        return minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(bucketName)
                        .object(file.getName())
                        .stream(file.getInputStream(), file.getSize(), -1L)
                        .contentType(file.getContentType())
                        .build()
        );
    }
}