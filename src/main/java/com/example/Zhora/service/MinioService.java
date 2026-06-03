package com.example.Zhora.service;

import com.example.Zhora.entity.FileConversionInbox;
import io.minio.GenericResponse;
import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;

public interface MinioService {

    InputStream downloadObject(FileConversionInbox fileConversion) throws MinioException, IOException;

    GenericResponse statObject(FileConversionInbox fileConversion) throws MinioException, IOException;

    GenericResponse putObject(MultipartFile file, String bucketName) ;
}
