package com.example.Zhora.service.repository;


import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.repository.FileConversionInboxRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileConversionInboxService {

    private final FileConversionInboxRepository fileConversionInboxRepository;

    public UUID findUuidByBucketNameAndName(String bucket, String fileName, String toExtension) {
        return fileConversionInboxRepository.findUuidByBucketNameAndNameAndToExtension(bucket, fileName, toExtension).orElse(null);
    }

    @Transactional
    public FileConversionInbox save(FileConversionInbox fileConversion) {
        return fileConversionInboxRepository.save(fileConversion);
    }

    public List<FileConversionInbox> findFilesNoConversion(int count) {
        return fileConversionInboxRepository.findFilesNoConversion(count);
    }

    public FileConversionInbox findById(UUID id) {
        return fileConversionInboxRepository.findById(UUID.fromString(id.toString())).orElse(null);
    }
}
