package com.example.Zhora.service.impl;

import com.example.Zhora.enums.FileExtension;
import com.example.Zhora.service.repository.MinioServiceImpl;
import org.apache.pdfbox.pdmodel.PDDocument;
import org.apache.pdfbox.pdmodel.PDPage;
import org.apache.pdfbox.pdmodel.PDPageContentStream;
import org.apache.pdfbox.pdmodel.common.PDRectangle;
import org.apache.pdfbox.pdmodel.graphics.image.LosslessFactory;
import org.apache.pdfbox.pdmodel.graphics.image.PDImageXObject;
import org.springframework.stereotype.Service;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;

import static com.example.Zhora.service.constant.ConstantConversion.IMAGE_DIVIDER;

@Service
public class ConversionImageServiceImpl extends AbstractConversionServiceImpl{

    public ConversionImageServiceImpl(MinioServiceImpl minioService) {
        super(minioService);
    }

    @Override
    public byte[] parsing(InputStream inputStream) throws IOException {
        try (PDDocument pdfDoc = new PDDocument();
             ByteArrayOutputStream out = new ByteArrayOutputStream()
        ) {
            PDPage page = new PDPage(PDRectangle.A4);
            pdfDoc.addPage(page);

            BufferedImage bufferedImage = ImageIO.read(inputStream);

            PDImageXObject pdImage = LosslessFactory.createFromImage(pdfDoc, bufferedImage);

            float pageWidth = page.getMediaBox().getWidth();
            float pageHeight = page.getMediaBox().getHeight();
            float imgWidth = pdImage.getWidth();
            float imgHeight = pdImage.getHeight();

            float scale = Math.min(pageWidth / imgWidth, pageHeight / imgHeight);
            float finalWidth = imgWidth * scale;
            float finalHeight = imgHeight * scale;

            float x = (pageWidth - finalWidth) / IMAGE_DIVIDER;
            float y = (pageHeight - finalHeight) / IMAGE_DIVIDER;

            try (PDPageContentStream contentStream = new PDPageContentStream(
                    pdfDoc, page, PDPageContentStream.AppendMode.APPEND, true, true)) {
                contentStream.drawImage(pdImage, x, y, finalWidth, finalHeight);
            }

            pdfDoc.save(out);
            return out.toByteArray();
        }
    }

    public boolean checkAllowedTypes(String from, String to) {
        if(FileExtension.JPG.getExtension().equals(from) || FileExtension.PNG.getExtension().equals(from)) {
            return FileExtension.JPG.getAllowedTypes().contains(to);
        }
        return false;
    }
}
