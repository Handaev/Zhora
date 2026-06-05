package com.example.Zhora.base;

import io.minio.BucketExistsArgs;
import io.minio.MakeBucketArgs;
import io.minio.MinioClient;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.MinIOContainer;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.kafka.KafkaContainer;
import org.testcontainers.utility.DockerImageName;

@SpringBootTest
public abstract class BaseIntegrationTest {

    static final PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15-alpine");

    static final KafkaContainer kafka = new KafkaContainer(DockerImageName.parse("apache/kafka:3.7.0"));

    static final MinIOContainer minio = new MinIOContainer("minio/minio:RELEASE.2025-09-07T16-13-09Z");

    static {
        postgres.start();
        kafka.start();
        minio.start();

        try {
            initMinioBuckets();
        } catch (Exception e) {
            throw new RuntimeException("Не удалось инициализировать бакеты MinIO в базовом классе", e);
        }
    }

    private static void initMinioBuckets() throws Exception {
        MinioClient initClient = MinioClient.builder()
                .endpoint(minio.getS3URL())
                .credentials(minio.getUserName(), minio.getPassword())
                .build();

        String[] buckets = {"test", "pdf"};

        for (String bucket : buckets) {
            boolean exists = initClient.bucketExists(BucketExistsArgs.builder().bucket(bucket).build());
            if (!exists) {
                initClient.makeBucket(MakeBucketArgs.builder().bucket(bucket).build());
            }
        }
    }

    @DynamicPropertySource
    static void configureProperties(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", postgres::getJdbcUrl);
        registry.add("spring.datasource.username", postgres::getUsername);
        registry.add("spring.datasource.password", postgres::getPassword);
        registry.add("spring.jpa.hibernate.ddl-auto", () -> "update");

        registry.add("kafka.bootstrap-servers", kafka::getBootstrapServers);

        registry.add("minio.url", minio::getS3URL);
        registry.add("minio.access-key", minio::getUserName);
        registry.add("minio.secret-key", minio::getPassword);
    }
}
