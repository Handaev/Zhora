package com.example.Zhora.repository;

import com.example.Zhora.entity.FileConversionOutbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileConversionOutboxRepository extends JpaRepository<FileConversionOutbox, UUID> {

    @Query("select f.id from FileConversionOutbox f where f.name = ?1 and f.bucketName = ?2")
    Optional<UUID> findUuidByNameAndBucketName(String name, String bucketName);

    @Query(value = "select * from file_conversion_outbox where is_sent = false limit ?1", nativeQuery = true)
    List<FileConversionOutbox> findAll(int limit);
}
