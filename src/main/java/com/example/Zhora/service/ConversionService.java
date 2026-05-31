package com.example.Zhora.service;

import com.example.Zhora.entity.FileConversion;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.record.ConversionRequestRecord;
import com.itextpdf.text.DocumentException;
import io.minio.errors.MinioException;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

public interface ConversionService {

    MultipartFile convertFile(FileConversion fileConversion) throws ConversionException;

    byte[] parsing(InputStream inputStream, String fileName) throws IOException;

    boolean checkConversionFile(FileConversion file) throws NoSuchFileException;
}
