package com.example.Zhora.service.repository;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.service.MinioService;
import io.minio.GetObjectArgs;
import io.minio.MinioClient;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import io.minio.PutObjectArgs;
import io.minio.ObjectWriteResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

import static com.example.Zhora.service.constant.ConstantConversion.AUTO_PART_SIZE;

@Slf4j
@Service
@RequiredArgsConstructor
public class MinioServiceImpl implements MinioService {

    private final MinioClient minioClient;

    @Override
    public InputStream downloadObject(FileConversionInbox fileConversion) {
        String fileName = fileConversion.getName();

        try {
            return minioClient.getObject(
                    GetObjectArgs.builder()
                            .bucket(fileConversion.getBucketName())
                            .object(fileName)
                            .build());
        } catch (MinioException e) {
            throw new ConversionException(String.format("Ошибка при получении файла %s", fileName), e);
        }
    }

    public StatObjectResponse statObject(FileConversionInbox fileConversion) {
        String fileName = fileConversion.getName();

        try {
            return minioClient.statObject(
                    StatObjectArgs.builder()
                            .bucket(fileConversion.getBucketName())
                            .object(fileName)
                            .build()
            );
        }
        catch (MinioException e) {
            throw new ConversionException(String.format("Ошибка при получении файла %s", fileName), e);
        }
    }

    @Override
    public ObjectWriteResponse putObject(MultipartFile file, String bucketName) {
        String fileName = file.getName();

        try {
            return minioClient.putObject(
                    PutObjectArgs.builder()
                            .bucket(bucketName)
                            .object(fileName)
                            .stream(file.getInputStream(), file.getSize(), AUTO_PART_SIZE)
                            .contentType(file.getContentType())
                            .build()
            );
        } catch (MinioException e) {
            throw new ConversionException(String.format("Ошибка при обновлении файла %s", fileName), e);
        } catch (IOException e) {
            throw new ConversionException(String.format("Ошибка при получении потока %s", fileName), e);
        }
    }
}