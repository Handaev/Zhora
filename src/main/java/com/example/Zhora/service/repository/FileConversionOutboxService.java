package com.example.Zhora.service.repository;


import com.example.Zhora.entity.FileConversionOutbox;
import com.example.Zhora.repository.FileConversionOutboxRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.UUID;

@Service
@RequiredArgsConstructor
public class FileConversionOutboxService {

    private final FileConversionOutboxRepository fileConversionOutboxRepository;

    public UUID findUuidByNameAndBucketName(String name, String bucketName) {
        return fileConversionOutboxRepository.findUuidByNameAndBucketName(name, bucketName).orElse(null);
    }

    public FileConversionOutbox save(FileConversionOutbox fileConversionOutbox) {
        return fileConversionOutboxRepository.save(fileConversionOutbox);
    }

    @Transactional
    public void save(List<FileConversionOutbox> fileConversionOutbox) {
        fileConversionOutboxRepository.saveAll(fileConversionOutbox);
    }

    public List<FileConversionOutbox> findAll(int limit) {
        return fileConversionOutboxRepository.findAll(limit);
    }
}