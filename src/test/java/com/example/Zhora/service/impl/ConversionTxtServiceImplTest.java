package com.example.Zhora.service.impl;

import com.example.Zhora.service.repository.MinioServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import static org.assertj.core.api.Assertions.assertThat;

@ExtendWith(MockitoExtension.class)
class ConversionTxtServiceImplTest {

    @Mock
    private MinioServiceImpl minioService;

    private ConversionTxtServiceImpl conversionService;

    @BeforeEach
    void setUp() {
        conversionService = new ConversionTxtServiceImpl(minioService);
    }

    @Test
    void parsing_WhenValidTextStream_ShouldReturnPdfByteArray() throws IOException {
        String textContent = "Hello World\nTest line\twith tab";
        InputStream inputStream = new ByteArrayInputStream(textContent.getBytes(StandardCharsets.UTF_8));

        byte[] result = conversionService.parsing(inputStream);

        assertThat(result).isNotNull();
        assertThat(result.length).isGreaterThan(0);

        String pdfHeader = new String(result, 0, 4);
        assertThat(pdfHeader).isEqualTo("%PDF");
    }

    @Test
    void checkAllowedTypes_WhenTxtToPdf_ShouldReturnTrue() {
        boolean result = conversionService.checkAllowedTypes("txt", "pdf");
        assertThat(result).isTrue();
    }

    @Test
    void checkAllowedTypes_WhenUnsupportedFromExtension_ShouldReturnFalse() {
        boolean result = conversionService.checkAllowedTypes("docx", "pdf");
        assertThat(result).isFalse();
    }

    @Test
    void checkAllowedTypes_WhenUnsupportedToExtension_ShouldReturnFalse() {
        boolean result = conversionService.checkAllowedTypes("txt", "docx");
        assertThat(result).isFalse();
    }
}
