package com.example.Zhora.service.impl;

import com.example.Zhora.service.repository.MinioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import javax.imageio.ImageIO;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConversionZipServiceImplTest {

    @Mock
    private MinioServiceImpl minioService;

    private ConversionZipServiceImpl conversionService;

    @BeforeEach
    void setUp() {
        conversionService = new ConversionZipServiceImpl(minioService);
    }

    @Test
    void parsing_WhenZipHasVariousEntries_ShouldReturnCombinedPdfByteArray() throws IOException {
        ByteArrayOutputStream zipByteStream = new ByteArrayOutputStream();
        try (ZipOutputStream zos = new ZipOutputStream(zipByteStream)) {

            zos.putNextEntry(new ZipEntry("test-dir/"));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("doc.txt"));
            zos.write("Hello inside ZIP".getBytes(StandardCharsets.UTF_8));
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("picture.png"));
            BufferedImage bufferedImage = new BufferedImage(10, 10, BufferedImage.TYPE_INT_RGB);
            ImageIO.write(bufferedImage, "png", zos);
            zos.closeEntry();

            zos.putNextEntry(new ZipEntry("unknown.dat"));
            zos.write(new byte[]{1, 2, 3});
            zos.closeEntry();
        }

        InputStream inputStream = new ByteArrayInputStream(zipByteStream.toByteArray());

        byte[] result = conversionService.parsing(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);

        String pdfHeader = new String(result, 0, 4);
        assertThat(pdfHeader).isEqualTo("%PDF");
    }

    @Test
    void checkAllowedTypes_WhenZipToPdf_ShouldReturnTrue() {
        boolean result = conversionService.checkAllowedTypes("zip", "pdf");
        assertThat(result).isTrue();
    }

    @Test
    void checkAllowedTypes_WhenUnsupportedFromExtension_ShouldReturnFalse() {
        boolean result = conversionService.checkAllowedTypes("rar", "pdf");
        assertThat(result).isFalse();
    }

    @Test
    void checkAllowedTypes_WhenUnsupportedToExtension_ShouldReturnFalse() {
        boolean result = conversionService.checkAllowedTypes("zip", "txt");
        assertThat(result).isFalse();
    }
}
