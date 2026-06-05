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

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConversionImageServiceImplTest {

    @Mock
    private MinioServiceImpl minioService;

    private ConversionImageServiceImpl conversionService;

    @BeforeEach
    void setUp() {
        conversionService = new ConversionImageServiceImpl(minioService);
    }

    @Test
    void parsing_WhenValidImageStream_ShouldReturnPdfByteArray() throws IOException {
        BufferedImage bufferedImage = new BufferedImage(100, 100, BufferedImage.TYPE_INT_RGB);
        ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
        ImageIO.write(bufferedImage, "png", outputStream);
        InputStream inputStream = new ByteArrayInputStream(outputStream.toByteArray());

        byte[] result = conversionService.parsing(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);

        String pdfHeader = new String(result, 0, 4);
        assertThat(pdfHeader).isEqualTo("%PDF");
    }

    @Test
    void checkAllowedTypes_WhenJpgToPdf_ShouldReturnTrue() {
        boolean result = conversionService.checkAllowedTypes("jpg", "pdf");
        assertThat(result).isTrue();
    }

    @Test
    void checkAllowedTypes_WhenPngToPdf_ShouldReturnTrue() {
        boolean result = conversionService.checkAllowedTypes("png", "pdf");
        assertThat(result).isTrue();
    }

    @Test
    void checkAllowedTypes_WhenUnsupportedFromExtension_ShouldReturnFalse() {
        boolean result = conversionService.checkAllowedTypes("txt", "pdf");
        assertThat(result).isFalse();
    }

    @Test
    void checkAllowedTypes_WhenUnsupportedToExtension_ShouldReturnFalse() {
        boolean result = conversionService.checkAllowedTypes("png", "txt");
        assertThat(result).isFalse();
    }
}
