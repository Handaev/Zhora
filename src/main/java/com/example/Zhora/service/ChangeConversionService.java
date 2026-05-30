package com.example.Zhora.service;


import com.example.Zhora.entity.FileConversion;
import io.minio.errors.MinioException;

import java.nio.file.NoSuchFileException;

public interface ChangeConversionService {

    ConversionService changeConversion(FileConversion fileConversion) throws MinioException, NoSuchFileException;
}
