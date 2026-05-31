package com.example.Zhora.service.impl;

import com.example.Zhora.entity.FileConversion;
import com.example.Zhora.enums.FileExtension;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.record.ConversionMultipartFile;
import com.example.Zhora.service.repository.MinioServiceImpl;
import com.itextpdf.text.*;
import com.itextpdf.text.pdf.PdfWriter;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.*;
import java.nio.charset.StandardCharsets;

import static com.example.Zhora.constant.ConversionConstant.DEFAULT_SIZE_FONT_FROM_TXT_TO_PDF;

@Service
public class ConversionTxtToPdfServiceImpl extends AbstractConversionServiceImpl {

    public ConversionTxtToPdfServiceImpl(MinioServiceImpl minioService) {
        super(minioService);
    }

    @Override
    public MultipartFile convertFile(FileConversion fileConversion) throws ConversionException {
        String fileName = fileConversion.getName();

        try (InputStream inputStream = minioService.downloadFile(fileConversion)) {

            String newNameFile  = fileName.replace(
                    FileExtension.TXT.getExtension(),
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
        Document pdfDoc = new Document(PageSize.A4);
        try (BufferedReader bufferedReader = new BufferedReader(
                new InputStreamReader(inputStream, StandardCharsets.UTF_8));
             ByteArrayOutputStream outputStream = new ByteArrayOutputStream()
        ) {
            PdfWriter.getInstance(pdfDoc, outputStream).setPdfVersion(PdfWriter.VERSION_1_7);
            pdfDoc.open();

            Font font = new Font();
            font.setStyle(Font.NORMAL);
            font.setSize(DEFAULT_SIZE_FONT_FROM_TXT_TO_PDF);

            String strLine;
            while ((strLine = bufferedReader.readLine()) != null) {
                Paragraph paragraph = new Paragraph(strLine + "\n", font);
                paragraph.setAlignment(Paragraph.ALIGN_JUSTIFIED);
                pdfDoc.add(paragraph);
            }
            
            return outputStream.toByteArray();
        } catch (DocumentException e) {
            throw new ConversionException(String.format("Ошибка при создании PDF файла %s", fileName), e);
        } finally {
            pdfDoc.close();
        }
    }
}