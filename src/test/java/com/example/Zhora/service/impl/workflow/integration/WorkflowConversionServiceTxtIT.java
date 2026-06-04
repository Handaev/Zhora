package com.example.Zhora.service.impl.workflow.integration;

import com.example.Zhora.base.BaseIntegrationTest;
import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.record.ConversionMultipartFile;
import com.example.Zhora.repository.FileConversionInboxRepository;
import com.example.Zhora.service.impl.workflow.WorkflowConversionServiceImpl;
import io.minio.MinioClient;
import io.minio.PutObjectArgs;
import io.minio.errors.MinioException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

import static org.assertj.core.api.AssertionsForClassTypes.assertThat;
import static org.assertj.core.api.AssertionsForClassTypes.assertThatThrownBy;

@SpringBootTest
public class WorkflowConversionServiceTxtIT extends BaseIntegrationTest {

    @Autowired
    private WorkflowConversionServiceImpl workflowConvertService;

    @Autowired
    private FileConversionInboxRepository fileConversionInboxRepository;

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
    public void testFullWorkflow_ShouldResponseSuccessfully() throws Exception {
        List<Future<ConversionMultipartFile>> futures =
                workflowConvertService.workflowConvert();

        Future<ConversionMultipartFile> future = futures.get(futures.size() - 1);

        String name = "4.pdf";
        String fromBucket = "test";
        String fileName = "4.txt";
        String toExtension = "pdf";
        String fromExtension = "txt";

        ConversionMultipartFile actualFile = future.get();

        assertThat(actualFile).isNotNull();

        assertThat(actualFile.getName()).isEqualTo(name);
        assertThat(actualFile.getOriginalFilename()).isEqualTo(fileName);
        assertThat(actualFile.getFromBucketName()).isEqualTo(fromBucket);
        assertThat(actualFile.getFromExtension()).isEqualTo(fromExtension);
        assertThat(actualFile.getToExtension()).isEqualTo(toExtension);

        assertThat(actualFile.getBytes()).isNotEmpty();
    }

    @Test
    public void testFullWorkflow_ShouldResponseConversionException_NoUpdateFile() throws Exception {
        String fileName = "423.txt";

        FileConversionInbox fakeFile = new FileConversionInbox();
        fakeFile.setName(fileName);
        fakeFile.setBucketName("test");
        fakeFile.setToExtension("pdf");

        fileConversionInboxRepository.save(fakeFile);

        List<Future<ConversionMultipartFile>> futures =
                workflowConvertService.workflowConvert();

        Future<ConversionMultipartFile> errorFuture = futures.get(futures.size() - 1);

        assertThatThrownBy(errorFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(ConversionException.class)
                .hasMessageContaining(String.format("Ошибка при получении файла %s", fileName));
    }

    @Test
    public void testFullWorkflow_ShouldResponseConversionException_NoSupportConversion() throws Exception {
        String fileName = "4.txt";
        String fromExtension = "txt";
        String toExtension = "png";

        FileConversionInbox fakeFile = new FileConversionInbox();
        fakeFile.setName(fileName);
        fakeFile.setBucketName("test");
        fakeFile.setToExtension(toExtension);

        fileConversionInboxRepository.save(fakeFile);

        List<Future<ConversionMultipartFile>> futures =
                workflowConvertService.workflowConvert();

        Future<ConversionMultipartFile> errorFuture = futures.get(futures.size() - 1);

        assertThatThrownBy(errorFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(ConversionException.class)
                .hasMessageContaining(String.format("No support conversion from %s to %s", fromExtension, toExtension));
    }

}