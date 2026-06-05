package com.example.Zhora.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Column;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.AllArgsConstructor;
import lombok.ToString;
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
        } else if(Objects.isNull(o) || getClass() != o.getClass()) {
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
