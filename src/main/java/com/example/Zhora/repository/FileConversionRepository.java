package com.example.Zhora.repository;

import com.example.Zhora.entity.FileConversion;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileConversionRepository extends JpaRepository<FileConversion, Long> {

    Optional<FileConversion> findFileByBucketNameAndNameAndToExtension(String bucketName, String name, String toExtension);


    @Query(value = "select f.id from FileConversion f where f.bucketName = ?1 " +
            "and f.name = ?2 " +
            "and f.toExtension = ?3")
    Optional<UUID> findUuidByBucketNameAndNameAndToExtension(String bucketName, String name, String toExtension);

    @Query(value = "select * from file_conversion where is_conversion = false limit ?1", nativeQuery = true)
    List<FileConversion> findFilesNoConversion(int count);
}
