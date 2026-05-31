package com.example.Zhora.service.impl;

import com.example.Zhora.entity.FileConversion;
import com.example.Zhora.enums.FileExtension;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.record.ConversionMultipartFile;
import com.example.Zhora.service.repository.MinioServiceImpl;
import com.itextpdf.text.Document;
import com.itextpdf.text.DocumentException;
import com.itextpdf.text.Image;
import com.itextpdf.text.PageSize;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URL;


@Service
public class ConversionJpgToPdfServiceImpl extends AbstractConversionServiceImpl{

    private final MinioServiceImpl minioServiceImpl;

    public ConversionJpgToPdfServiceImpl(MinioServiceImpl minioService,
                                         MinioServiceImpl minioServiceImpl) {
        super(minioService);
        this.minioServiceImpl = minioServiceImpl;
    }

    @Override
    public MultipartFile convertFile(FileConversion fileConversion) throws ConversionException {
        String fileName = fileConversion.getName();

        try(InputStream inputStream = minioServiceImpl.downloadFile(fileConversion)) {

            String newNameFile  = fileName.replace(
                    FileExtension.JPG.getExtension(),
                    FileExtension.PDF.getExtension()
            );

            return new ConversionMultipartFile(
                    newNameFile,
                    parsing(inputStream, newNameFile),
                    newNameFile.replace(".", "/")
            );

        }catch (IOException e) {
            throw new ConversionException(String.format("Ошибка при чтении или записи из бакета %s файла %s",
                    fileConversion.getBucketName(), fileName), e);
        }
    }

    @Override
    public byte[] parsing(InputStream inputStream, String fileName) throws IOException {
        Document pdfDoc = new Document(PageSize.A4);
        try (ByteArrayOutputStream outputStream = new ByteArrayOutputStream()) {
            PdfWriter writer = PdfWriter.getInstance(pdfDoc, outputStream);
            writer.open();
            pdfDoc.open();

            pdfDoc.add(Image.getInstance(new URL(fileName)));

            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new ConversionException(String.format("Ошибка при создании PDF файла %s", fileName), e);
        } finally {
            pdfDoc.close();
        }
    }
}
