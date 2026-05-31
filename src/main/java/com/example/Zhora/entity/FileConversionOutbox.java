package com.example.Zhora.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@Entity
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_conversion_outbox")
public class FileConversionOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private Long id;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(nullable = false)
    private String name;
}
