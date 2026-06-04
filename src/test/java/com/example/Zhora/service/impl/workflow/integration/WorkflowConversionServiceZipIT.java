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
import org.assertj.core.api.AssertionsForClassTypes;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.io.ClassPathResource;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@SpringBootTest
public class WorkflowConversionServiceZipIT extends BaseIntegrationTest {

    @Autowired
    private WorkflowConversionServiceImpl workflowConvertService;

    @Autowired
    private FileConversionInboxRepository fileConversionInboxRepository;

    @Autowired
    private MinioClient minioClient;

    private FileConversionInbox file = new FileConversionInbox();

    @BeforeEach
    public void setUp() throws IOException, MinioException {
        fileConversionInboxRepository.deleteAll();

        String fileName = "4.zip";
        String fromBucketName = "test";
        String toExtension = "pdf";
        String contentType = "4/pdf";
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
    public void testFullWorkflow_ShouldConvertZipSuccessfully() throws Exception {
        String zipName = "4.zip";
        FileConversionInbox zipInbox = new FileConversionInbox();
        zipInbox.setName(zipName);
        zipInbox.setBucketName("test");
        zipInbox.setToExtension("pdf");
        fileConversionInboxRepository.save(zipInbox);

        java.io.ByteArrayOutputStream baos = new java.io.ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(baos)) {
            zos.putNextEntry(new ZipEntry("internal.txt"));
            zos.write("Text inside zip".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();
        }
        byte[] zipContent = baos.toByteArray();

        minioClient.putObject(
                PutObjectArgs.builder()
                        .bucket("test")
                        .object(zipName)
                        .stream(new ByteArrayInputStream(zipContent), (long) zipContent.length, -1L)
                        .contentType("4/pdf")
                        .build()
        );

        List<Future<ConversionMultipartFile>> futures = workflowConvertService.workflowConvert();
        ConversionMultipartFile actualFile = futures.get(futures.size() - 1).get();

        assertThat(actualFile).isNotNull();
        assertThat(actualFile.getName()).isEqualTo("4.pdf");
    }

    @Test
    public void testFullWorkflow_ShouldResponseConversionException_NoUpdateFile() throws InterruptedException {
        String zipName = "missing_archive.zip";
        FileConversionInbox fakeZipInbox = new FileConversionInbox();
        fakeZipInbox.setName(zipName);
        fakeZipInbox.setBucketName("test");
        fakeZipInbox.setToExtension("pdf");
        fileConversionInboxRepository.save(fakeZipInbox);

        List<Future<ConversionMultipartFile>> futures = workflowConvertService.workflowConvert();
        Future<ConversionMultipartFile> errorFuture = futures.get(futures.size() - 1);

        assertThatThrownBy(errorFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(ConversionException.class)
                .hasMessageContaining(String.format("Ошибка при получении файла %s", zipName));
    }

    @Test
    public void testFullWorkflow_ShouldResponseConversionException_NoSupportConversion() throws InterruptedException {
        String zipName = "4.zip";
        FileConversionInbox fakeZipInbox = new FileConversionInbox();
        fakeZipInbox.setName(zipName);
        fakeZipInbox.setBucketName("test");
        fakeZipInbox.setToExtension("docx");
        fileConversionInboxRepository.save(fakeZipInbox);

        List<Future<ConversionMultipartFile>> futures =
                workflowConvertService.workflowConvert();

        Future<ConversionMultipartFile> errorFuture = futures.get(futures.size() - 1);

        AssertionsForClassTypes.assertThatThrownBy(errorFuture::get)
                .isInstanceOf(ExecutionException.class)
                .hasCauseInstanceOf(ConversionException.class)
                .hasMessageContaining("No support conversion from zip to docx");
    }
}
