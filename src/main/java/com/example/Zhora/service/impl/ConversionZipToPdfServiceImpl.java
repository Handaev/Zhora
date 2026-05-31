package com.example.Zhora.service.impl;


import com.example.Zhora.entity.FileConversion;
import com.example.Zhora.enums.FileExtension;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.record.ConversionMultipartFile;
import com.example.Zhora.service.repository.MinioServiceImpl;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

@Service
public class ConversionZipToPdfServiceImpl extends AbstractConversionServiceImpl{

    private final MinioServiceImpl minioServiceImpl;

    public ConversionZipToPdfServiceImpl(MinioServiceImpl minioService,
                                         MinioServiceImpl minioServiceImpl) {
        super(minioService);
        this.minioServiceImpl = minioServiceImpl;
    }

    @Override
    public MultipartFile convertFile(FileConversion fileConversion) throws ConversionException {
        String fileName = fileConversion.getName();

        try (InputStream inputStream = minioService.downloadFile(fileConversion)) {

            String newNameFile  = fileName.replace(
                    FileExtension.ZIP.getExtension(),
                    FileExtension.PDF.getExtension()
            );

            return new ConversionMultipartFile(
                    newNameFile,
                    parsing(inputStream, fileName),
                    newNameFile.replace(".", "/")
            );

        } catch (IOException e) {
            throw new ConversionException(String.format("Ошибка при чтении или записи из бакета %s файла %s",
                    fileConversion.getBucketName(), fileName), e);
        }
    }

    @Override
    public byte[] parsing(InputStream inputStream, String fileName) throws IOException {
        try (PDDocument document = new PDDocument();
             ZipInputStream zis = new ZipInputStream(inputStream)
        ) {
            ZipEntry zipEntry;

            while((zipEntry = zis.getNextEntry()) != null) {
                if(zipEntry.isDirectory()) {
                    continue;
                }
                String nameFile = zipEntry.getName().toLowerCase();

                byte[] dataFile = zis.readAllBytes();

                if(nameFile.endsWith(FileExtension.PDF.getExtension())
                        || nameFile.endsWith(FileExtension.JPG.getExtension())) {

                }

            }

        }
        return new byte[10];

    }
}
