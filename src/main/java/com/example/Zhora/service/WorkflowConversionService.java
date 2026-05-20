package com.example.Zhora.service;


import com.example.Zhora.enums.FileExtension;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;
import java.util.ConcurrentModificationException;


@Slf4j
@Service
@RequiredArgsConstructor
public class WorkflowConversionService {

    private final MinioService minioService;


//    public void conversionFile(String fileName, String toExtension) {
//        if(checkConversionFile(fileName, toExtension)){
//            try {
//                InputStream inputStream = minioService.downloadFile(fileName);
//            } catch (MinioException e) {
//                throw new RuntimeException(e);
//            } catch (IOException e) {
//                throw new RuntimeException(e);
//            }
//        }
//    }

    public boolean checkConversionFile(String fileName, String toExtension) {
        try {
            StatObjectResponse statObjectResponse = minioService.getMetadataFile(fileName);
            String fromExtension = statObjectResponse.contentType().split("/")[1];
            if(checkAllowedTypes(fromExtension, toExtension)) {
                return true;
            } else {
                String message = String.format("Ошибка конвертации: Нельзя конвертировать %s -> %s", fromExtension, toExtension);
                log.debug(message);
                throw new ConcurrentModificationException(message);
            }
        } catch (NoSuchFileException e) {
            log.debug(e.getMessage());
            return false;
        } catch (MinioException e) {
            log.debug("Ошибка при получении метаданных файла " + fileName);
            return false;
        }
    }

    private boolean checkAllowedTypes(String from, String to) {
        return FileExtension.getFileExtension(from).getAllowedTypes()
                .contains(FileExtension.getFileExtension(to));
    }
}
