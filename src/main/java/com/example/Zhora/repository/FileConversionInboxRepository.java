package com.example.Zhora.repository;

import com.example.Zhora.entity.FileConversionInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileConversionInboxRepository extends JpaRepository<FileConversionInbox, UUID> {

    @Query(value = "select f.id from FileConversionInbox f where f.bucketName = :bucketName and f.name = :name and f.toExtension = :toExtension")
    Optional<UUID> findUuidByBucketNameAndNameAndToExtension(
            String bucketName, String name, String toExtension);

    @Query(value = "select * from file_conversion_inbox where is_conversion = false limit :count", nativeQuery = true)
    List<FileConversionInbox> findFilesNoConversion(int count);
}