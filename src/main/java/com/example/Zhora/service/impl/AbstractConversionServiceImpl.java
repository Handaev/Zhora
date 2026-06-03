package com.example.Zhora.service.impl;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.enums.FileExtension;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.record.ConversionMultipartFile;
import com.example.Zhora.service.ConversionService;
import com.example.Zhora.service.repository.MinioServiceImpl;
import io.minio.StatObjectResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractConversionServiceImpl implements ConversionService {

    protected final MinioServiceImpl minioServiceImpl;

    public ConversionMultipartFile convert(FileConversionInbox fileConversion) throws ConversionException {
        String fileName = fileConversion.getName();

        try(InputStream inputStream = minioServiceImpl.downloadObject(fileConversion)) {

            String newNameFile  = fileName.replace(
                    fileName.split("\\.")[1],
                    FileExtension.PDF.getExtension()
            );

            return new ConversionMultipartFile(
                    fileConversion.getId(),
                    newNameFile,
                    fileName,
                    fileConversion.getBucketName(),
                    parsing(inputStream),
                    newNameFile.replace(".", "/"),
                    fileName.split("\\.")[1],
                    FileExtension.PDF.getExtension()
            );

        } catch (IOException e) {
            throw new ConversionException(String.format("Ошибка при парсинге из бакета %s файла %s",
                    fileConversion.getBucketName(), fileName), e);
        }
    }

    public boolean checkConversion(FileConversionInbox file) throws ConversionException, NoSuchFileException {
        StatObjectResponse statObjectResponse = minioServiceImpl.statObject(file);
        String fromExtension = statObjectResponse.object().split("\\.")[1];
        return checkAllowedTypes(fromExtension, file.getToExtension());
    }
}
