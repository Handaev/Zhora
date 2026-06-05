package com.example.Zhora.service.impl;

import com.example.Zhora.enums.FileExtension;
import com.example.Zhora.service.repository.MinioServiceImpl;
import lombok.extern.slf4j.Slf4j;
import org.apache.pdfbox.Loader;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.font.PDFont;
import org.apache.pdfbox.pdmodel.font.PDType1Font;
import org.apache.pdfbox.pdmodel.font.Standard14Fonts;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import java.io.BufferedReader;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.ByteArrayInputStream;
import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.nio.charset.StandardCharsets;
import java.util.Objects;
import java.util.zip.ZipEntry;
import java.util.zip.ZipInputStream;

import static com.example.Zhora.service.constant.ConstantConversion.DIRECTORY_FONT_SIZE;
import static com.example.Zhora.service.constant.ConstantConversion.SIZE_BUFFER;
import static com.example.Zhora.service.constant.ConstantConversion.ERROR_PROCESSING;
import static com.example.Zhora.service.constant.ConstantConversion.DIRECTORY_X_START;
import static com.example.Zhora.service.constant.ConstantConversion.DIRECTORY_Y_START;
import static com.example.Zhora.service.constant.ConstantConversion.PRE_DIRECTORY;
import static com.example.Zhora.service.constant.ConstantConversion.IMAGE_X_START;
import static com.example.Zhora.service.constant.ConstantConversion.IMAGE_Y_START;
import static com.example.Zhora.service.constant.ConstantConversion.TEXT_FONT_SIZE;
import static com.example.Zhora.service.constant.ConstantConversion.TEXT_LEADING;
import static com.example.Zhora.service.constant.ConstantConversion.TEXT_X_START;
import static com.example.Zhora.service.constant.ConstantConversion.TEXT_Y_START;
import static com.example.Zhora.service.constant.ConstantConversion.PRE_FILE;
import static com.example.Zhora.service.constant.ConstantConversion.UNKNOWN_FONT_SIZE;
import static com.example.Zhora.service.constant.ConstantConversion.LINE_UNSUPPORTED;
import static com.example.Zhora.service.constant.ConstantConversion.UNKNOWN_X_START;
import static com.example.Zhora.service.constant.ConstantConversion.UNKNOWN_Y_START;
import static com.example.Zhora.service.constant.ConstantConversion.NON_CONVERTABLE;
import static com.example.Zhora.service.constant.ConstantConversion.PRE_NAME;
import static com.example.Zhora.service.constant.ConstantConversion.PRE_SIZE;
import static com.example.Zhora.service.constant.ConstantConversion.POST_BYTES;

@Slf4j
@Service
public class ConversionZipServiceImpl extends AbstractConversionServiceImpl{

    public ConversionZipServiceImpl(MinioServiceImpl minioService) {
        super(minioService);
    }

    @Override
    public byte[] parsing(InputStream inputStream) throws IOException {
        try (PDDocument finalPdf = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream();
             ZipInputStream zipStream = new ZipInputStream(inputStream)) {

            PDFont customFont = new PDType1Font(Standard14Fonts.FontName.HELVETICA);

            ZipEntry entry;
            while (Objects.nonNull((entry = zipStream.getNextEntry()))) {
                String name = entry.getName();

                if (entry.isDirectory()) {
                    processDirectoryPage(finalPdf, name, customFont);
                    zipStream.closeEntry();
                    continue;
                }

                ByteArrayOutputStream entryBytes = new ByteArrayOutputStream();
                byte[] buffer = new byte[SIZE_BUFFER];
                int bytesRead;
                while ((bytesRead = zipStream.read(buffer)) != -1) {
                    entryBytes.write(buffer, 0, bytesRead);
                }
                byte[] fileData = entryBytes.toByteArray();

                String lowerName = name.toLowerCase();
                try {
                    if (lowerName.endsWith(FileExtension.JPG.getExtension()) || lowerName.endsWith(FileExtension.PNG.getExtension())) {
                        processImagePage(finalPdf, fileData);
                    } else if (lowerName.endsWith(FileExtension.TXT.getExtension())) {
                        processTextPage(finalPdf, fileData, name, customFont);
                    } else if (lowerName.endsWith(FileExtension.PDF.getExtension())) {
                        processPdfPages(finalPdf, fileData);
                    } else {
                        processUnknownPage(finalPdf, name, fileData.length, customFont);
                    }
                } catch (Exception e) {
                    processUnknownPage(finalPdf, ERROR_PROCESSING, fileData.length, customFont);
                }

                zipStream.closeEntry();
            }

            finalPdf.save(out);
            return out.toByteArray();
        }
    }


    private void processDirectoryPage(PDDocument doc, String dirName, PDFont fontBold) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
            stream.beginText();
            stream.setFont(fontBold, DIRECTORY_FONT_SIZE);
            stream.newLineAtOffset(DIRECTORY_X_START, DIRECTORY_Y_START);
            stream.showText(PRE_DIRECTORY + dirName);
            stream.endText();
        }
    }

    private void processImagePage(PDDocument doc, byte[] bytes) throws IOException {
        try (ByteArrayInputStream bais = new ByteArrayInputStream(bytes)) {
            BufferedImage bimage = ImageIO.read(bais);
            if (Objects.nonNull(bimage)) {
                PDRectangle pageSize = new PDRectangle(bimage.getWidth(), bimage.getHeight());
                PDPage page = new PDPage(pageSize);
                doc.addPage(page);

                PDImageXObject img = LosslessFactory.createFromImage(doc, bimage);
                try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
                    stream.drawImage(img, IMAGE_X_START, IMAGE_Y_START);
                }
            }
        }
    }

    private void processTextPage(PDDocument doc, byte[] bytes, String fileName, PDFont font) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
            stream.beginText();
            stream.setFont(font, TEXT_FONT_SIZE);
            stream.setLeading(TEXT_LEADING);
            stream.newLineAtOffset(TEXT_X_START, TEXT_Y_START);

            stream.showText(PRE_FILE + fileName);
            stream.newLine();
            stream.newLine();

            String text = new String(bytes, StandardCharsets.UTF_8);
            BufferedReader reader = new BufferedReader(new StringReader(text));
            String line;
            int lines = 0;

            while (Objects.nonNull((line = reader.readLine()))  && lines < 40) {
                line = line.replace("\t", "    ").replaceAll("[\\p{Cntrl}&&[^\\r\\n\\t]]", "");
                try {
                    stream.showText(line);
                } catch (IllegalArgumentException e) {
                    stream.showText(LINE_UNSUPPORTED);
                }
                stream.newLine();
                lines++;
            }
            stream.endText();
        }
    }

    private void processPdfPages(PDDocument mainDoc, byte[] bytes) throws IOException {
        try (PDDocument innerDoc = Loader.loadPDF(bytes)) {
            for (PDPage page : innerDoc.getPages()) {
                mainDoc.addPage(mainDoc.importPage(page));
            }
        }
    }

    private void processUnknownPage(PDDocument doc, String fileName, long size, PDFont font) throws IOException {
        PDPage page = new PDPage(PDRectangle.A4);
        doc.addPage(page);
        try (PDPageContentStream stream = new PDPageContentStream(doc, page)) {
            stream.beginText();
            stream.setFont(font, UNKNOWN_FONT_SIZE);
            stream.newLineAtOffset(UNKNOWN_X_START, UNKNOWN_Y_START);
            stream.showText(NON_CONVERTABLE);
            stream.showText(PRE_NAME + fileName);
            stream.newLine();
            stream.showText(PRE_SIZE + size + POST_BYTES);
            stream.endText();
        }
    }

    @Override
    public boolean checkAllowedTypes(String from, String to) {
        if(FileExtension.ZIP.getExtension().equals(from)) {
            return FileExtension.ZIP.getAllowedTypes().contains(to);
        }
        return false;
    }
}