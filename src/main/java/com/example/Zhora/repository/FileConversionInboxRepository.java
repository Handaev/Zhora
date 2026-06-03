package com.example.Zhora.repository;

import com.example.Zhora.entity.FileConversionInbox;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface FileConversionInboxRepository extends JpaRepository<FileConversionInbox, UUID> {

    @Query(value = "select f.id from FileConversionInbox f where f.bucketName = ?1 " +
            "and f.name = ?2 " +
            "and f.toExtension = ?3")
    Optional<UUID> findUuidByBucketNameAndNameAndToExtension(String bucketName, String name, String toExtension);

    @Query(value = "select * from file_conversion_inbox where is_conversion = false limit ?1", nativeQuery = true)
    List<FileConversionInbox> findFilesNoConversion(int count);

//    @Modifying(clearAutomatically = true) // в чем была основная проблема
    @Query(value = "update FileConversionInbox f set f.conversion = true where f.id = :uuid")
    int updateConversionByUuid(@Param("uuid") UUID uuid);
}
