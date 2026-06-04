package com.example.Zhora.service.scheduler.integration;


import com.example.Zhora.base.BaseIntegrationTest;
import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.repository.FileConversionInboxRepository;
import com.example.Zhora.repository.FileConversionOutboxRepository;
import com.example.Zhora.service.scheduler.ConversionScheduler;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.StatObjectArgs;
import io.minio.StatObjectResponse;
import io.minio.errors.MinioException;
import org.junit.Before;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.ExecutionException;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class ConversionSchedulerITest extends BaseIntegrationTest {

    @Autowired
    private ConversionScheduler conversionScheduler;

    @Autowired
    private FileConversionInboxRepository fileConversionInboxRepository;

    @Autowired
    private FileConversionOutboxRepository fileConversionOutboxRepository;

    @Autowired
    private MinioClient minioClient;

    private FileConversionInbox file = new FileConversionInbox();

    @BeforeEach
    public void setUp() throws IOException, MinioException {
        String fileName = "4.txt";
        String fromBucketName = "test";
        String toExtension = "pdf";
        String contentType = "4/txt";
        String directory = "files/";

        file.setName(fileName);
        file.setBucketName(fromBucketName);
        file.setToExtension(toExtension);
        fileConversionInboxRepository.save(file);

        ClassPathResource resource = new ClassPathResource(directory + fileName);
        byte[] content = resource.getInputStream().readAllBytes();

        Long objectSize = (long) content.length;
        Long partSize = -1L;

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket(fromBucketName)
                        .object(fileName)
                        .stream(new ByteArrayInputStream(content), objectSize, partSize)
                        .contentType(contentType)
                        .build()
        );
    }

    @Test
    public void testFullWorkflow_ShouldResponseSuccessfully() throws MinioException {
        conversionScheduler.scheduleConversion();

        FileConversionInbox saved = fileConversionInboxRepository.findById(file.getId()).orElseThrow();
        assertThat(saved.isConversion()).isTrue();

        String toBucketName = "pdf";
        String toFileName = "4.pdf";
        StatObjectResponse minioFileStat = minioClient.statObject(
                StatObjectArgs.builder()
                        .bucket(toBucketName)
                        .object(toFileName)
                        .build()
        );

        assertThat(minioFileStat).isNotNull();
        assertThat(minioFileStat.size()).isGreaterThan(0);

        Optional<UUID> savedOutbox = fileConversionOutboxRepository.findUuidByNameAndBucketName(toFileName, toBucketName);

        assertThat(savedOutbox).isPresent();
    }
}
