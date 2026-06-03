package com.example.Zhora.record;

import lombok.Getter;
import lombok.Setter;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.util.UUID;

@Getter
@Setter
public class ConversionMultipartFile implements MultipartFile {

    private UUID FileConversionInboxUuid;

    private String name;

    private String originalFileName;

    private byte[] content;

    private String fromBucketName;

    private String toExtension;

    private String contentType;

    private String fromExtension;

    public ConversionMultipartFile(UUID id,
                                   String name,
                                   String originalFileName,
                                   String fromBucketName,
                                   byte[] content,
                                   String contentType,
                                   String toExtension,
                                   String fromExtension) {
        this.FileConversionInboxUuid = id;
        this.name = name;
        this.originalFileName = originalFileName;
        this.fromBucketName = fromBucketName;
        this.content = content;
        this.contentType = contentType;
        this.toExtension = toExtension;
        this.fromExtension = fromExtension;
    }

    @Override
    public String getOriginalFilename() {
        return originalFileName;
    }

    @Override
    public boolean isEmpty() {
        return content == null || content.length == 0;
    }

    @Override
    public long getSize() {
        return content.length;
    }

    @Override
    public byte[] getBytes() {
        return content;
    }

    @Override
    public InputStream getInputStream() {
        return new ByteArrayInputStream(content);
    }

    @Override
    public void transferTo(File dest) throws IllegalStateException {
    }
}
