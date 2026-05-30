package com.example.Zhora.enums;

import lombok.Getter;
import lombok.ToString;

import java.util.Set;

@Getter
@ToString
public enum FileExtension {
    NON("", Set.of()),
    PDF("pdf", Set.of()),
    TXT("txt", Set.of("pdf")),
    PNG("png", Set.of("pdf")),
    JPG("jpg", Set.of("pdf")),
    ZIP("zip", Set.of("pdf"));

    private final String extension;
    private Set<String> allowedTypes;

    FileExtension(String extension,
                  Set<Object> allowedTypes) {
        this.extension = extension;
    }

    public static FileExtension getFileExtension(String extension) {
        for (FileExtension fileExtension : FileExtension.values()) {
            if (fileExtension.extension.equals(extension)) {
                return fileExtension;
            }
        }
        return FileExtension.NON;
    }
}
