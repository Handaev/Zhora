package com.example.Zhora.enums;

import lombok.Getter;

import java.util.Set;

@Getter
public enum FileExtension {
    NON(null, Set.of()),
    TXT("txt", Set.of()),
    PNG("png", Set.of()),
    JPG("jpg", Set.of()),
    ZIP("zip", Set.of()),
    PDF("pdf", Set.of(TXT, PNG, JPG, ZIP));

    private final String extension;
    private Set<FileExtension> allowedTypes;

    FileExtension(String extension,
                  Set<Object> allowedTypes) {
        this.extension = extension;
    }

    @Override
    public String toString() {
        return extension;
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
