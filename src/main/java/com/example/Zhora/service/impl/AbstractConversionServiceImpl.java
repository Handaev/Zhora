package com.example.Zhora.service.impl;

import com.example.Zhora.entity.FileConversion;
import com.example.Zhora.enums.FileExtension;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.service.ConversionService;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import java.nio.file.NoSuchFileException;

@Slf4j
@RequiredArgsConstructor
public abstract class AbstractConversionServiceImpl implements ConversionService {

    protected final MinioServiceImpl minioService;

    public boolean checkConversionFile(FileConversion file) throws NoSuchFileException, ConversionException {
        StatObjectResponse statObjectResponse = minioService.statObject(file);
        String fromExtension = statObjectResponse.contentType().split("/")[1];
        return checkAllowedTypes(fromExtension, file.getToExtension());
    }

    private boolean checkAllowedTypes(String from, String to) {
        if(FileExtension.getFileExtension(from).getAllowedTypes().contains(to)) {
            return true;
        }

        return false;
    }
}
