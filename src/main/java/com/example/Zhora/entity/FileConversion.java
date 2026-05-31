package com.example.Zhora.entity;

import jakarta.persistence.*;
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
public class FileConversion {

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
    public boolean equals(Object object) {
        if(this == object) {
            return true;
        }

        if(object == null || getClass() != object.getClass()) {
            return false;
        }

        FileConversion fileConversion = (FileConversion) object;

        return this.name.equals(fileConversion.name) &&  this.bucketName.equals(fileConversion.bucketName);
    }

    @Override
    public int hashCode(){
        return Objects.hash(name, bucketName);
    }
}
