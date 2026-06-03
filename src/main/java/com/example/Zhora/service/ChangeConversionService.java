package com.example.Zhora.service;


import com.example.Zhora.entity.FileConversionInbox;
import io.minio.errors.MinioException;

import java.nio.file.NoSuchFileException;

public interface ChangeConversionService {
    ConversionService changeConversion(FileConversionInbox fileConversion) throws MinioException, NoSuchFileException;
}