package com.example.Zhora.service.impl;

import com.example.Zhora.enums.FileExtension;
import com.example.Zhora.service.repository.MinioServiceImpl;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.Objects;

import static com.example.Zhora.service.constant.ConstantConversion.*;
import static org.apache.pdfbox.pdmodel.font.Standard14Fonts.FontName.HELVETICA;

@Service
public class ConversionTxtServiceImpl extends AbstractConversionServiceImpl {

    public ConversionTxtServiceImpl(MinioServiceImpl minioService) {
        super(minioService);
    }

    @Override
    public byte[] parsing(InputStream inputStream) throws IOException {
        try (PDDocument pdfDoc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream, StandardCharsets.UTF_8))
        ) {
            PDPage page = new PDPage(PDRectangle.A4);
            pdfDoc.addPage(page);

            PDType1Font font = new PDType1Font(HELVETICA);
            float y = page.getMediaBox().getHeight() - TOP_MARGIN;

            try (PDPageContentStream contentStream = new PDPageContentStream(
                    pdfDoc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {

                contentStream.beginText();
                contentStream.setFont(font, TXT_FONT_SIZE);
                contentStream.setLeading(TXT_LEADING);
                contentStream.newLineAtOffset(TXT_X_START, y);

                String line;
                while (Objects.nonNull((line = reader.readLine()))) {
                    line = line.replace("\r", "").replace("\t", "    ");

                    contentStream.showText(line);
                    contentStream.newLine();
                }
                contentStream.endText();
            }

            pdfDoc.save(out);
            return out.toByteArray();
        }
    }


    @Override
    public boolean checkAllowedTypes(String from, String to) {
        if (FileExtension.TXT.getExtension().equals(from)) {
            return FileExtension.TXT.getAllowedTypes().contains(to);
        }
        return false;
    }
}