package com.example.Zhora.entity;

import jakarta.persistence.*;
import lombok.*;
import org.hibernate.annotations.ColumnDefault;

import java.util.Objects;
import java.util.UUID;

@Getter
@Setter
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@ToString
@Table(name = "file_conversion_outbox")
public class FileConversionOutbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bucket_name", nullable = false)
    private String bucketName;

    @Column(nullable = false)
    private String name;

    @ColumnDefault("false")
    @Column(name = "is_sent", nullable = false)
    private boolean sent;

    @Override
    public boolean equals(Object o) {
        if(o == this) {
            return true;
        } else if(o == null || getClass() != o.getClass()) {
            return false;
        }

        FileConversionOutbox fileConversionOutbox = (FileConversionOutbox) o;

        return this.name.equals(fileConversionOutbox.name)
                && this.bucketName.equals(fileConversionOutbox.bucketName);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name, bucketName);
    }
}
