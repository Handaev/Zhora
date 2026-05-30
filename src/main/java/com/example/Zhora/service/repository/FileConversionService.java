package com.example.Zhora.service.repository;


import com.example.Zhora.entity.FileConversion;
import com.example.Zhora.repository.FileConversionRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileConversionService {

    private final FileConversionRepository fileConversionRepository;

    public FileConversion findFileByBucketNameAndNameAndToExtension(String bucket, String fileName, String toExtension) {
        return fileConversionRepository.findFileByBucketNameAndNameAndToExtension(bucket, fileName, toExtension).orElse(null);
    }

    public UUID findUuidByBucketNameAndName(String bucket, String fileName, String toExtension) {
        return fileConversionRepository.findUuidByBucketNameAndNameAndToExtension(bucket, fileName, toExtension).orElse(null);
    }

    @Transactional
    public FileConversion save(FileConversion fileConversion) {
        return fileConversionRepository.save(fileConversion);
    }

    public List<FileConversion> findFilesNoConversion(int count) {
        return fileConversionRepository.findFilesNoConversion(count);
    }
}
