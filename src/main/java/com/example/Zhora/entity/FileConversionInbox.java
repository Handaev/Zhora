package com.example.Zhora.entity;

import jakarta.persistence.Entity;
import jakarta.persistence.GenerationType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Column;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.hibernate.annotations.ColumnDefault;

import java.util.Objects;
import java.util.UUID;

@Entity
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "file_conversion_inbox")
public class FileConversionInbox {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "bucket", nullable = false)
    private String bucketName;

    @Column(nullable = false)
    private String name;

    @Column(name = "to_extension", nullable = false)
    private String toExtension;

    @Column(name = "is_conversion", nullable = false)
    @ColumnDefault("false")
    private boolean conversion;

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (Objects.isNull(o) || getClass() != o.getClass()) {
            return false;
        }
        FileConversionInbox fileConversionInbox = (FileConversionInbox) o;

        return this.name.equals(fileConversionInbox.getName())
                && this.bucketName.equals(fileConversionInbox.getBucketName());
    }

    @Override
    public int hashCode() {
        return getClass().hashCode();
    }
}
