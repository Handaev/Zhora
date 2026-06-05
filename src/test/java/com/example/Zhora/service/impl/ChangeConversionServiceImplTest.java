package com.example.Zhora.service.impl;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.service.ConversionService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.any;

@ExtendWith(MockitoExtension.class)
class ChangeConversionServiceImplTest {

    @Mock
    private ConversionImageServiceImpl conversionImageService;

    @Mock
    private ConversionTxtServiceImpl conversionTxtService;

    private ChangeConversionServiceImpl changeConversionService;

    @BeforeEach
    void setUp() {
        List<ConversionService> conversionServices = new ArrayList<>(List.of(conversionImageService, conversionTxtService));
        changeConversionService = new ChangeConversionServiceImpl(conversionServices);
    }

    @Test
    void changeConversion_WhenServiceSupportsConversion_ShouldReturnService() throws Exception {
        FileConversionInbox file = new FileConversionInbox();
        file.setName("document.docx");
        file.setToExtension("pdf");

        when(conversionImageService.checkConversion(file)).thenReturn(false);
        when(conversionTxtService.checkConversion(file)).thenReturn(true);

        ConversionService result = changeConversionService.changeConversion(file);

        assertThat(result).isSameAs(conversionTxtService);
        verify(conversionImageService, times(1)).checkConversion(file);
        verify(conversionTxtService, times(1)).checkConversion(file);
    }

    @Test
    void changeConversion_WhenNoSuchFileExceptionOccurs_ShouldThrowConversionException() throws Exception {
        FileConversionInbox file = new FileConversionInbox();
        file.setName("missing.docx");
        file.setToExtension("pdf");

        when(conversionImageService.checkConversion(file)).thenThrow(new ConversionException("File not found"));

        assertThatThrownBy(() -> changeConversionService.changeConversion(file))
                .isInstanceOf(ConversionException.class);

        verify(conversionTxtService, never()).checkConversion(any());
    }

    @Test
    void changeConversion_WhenNoServiceSupportsConversion_ShouldThrowConversionException() throws Exception {
        FileConversionInbox file = new FileConversionInbox();
        file.setName("image.png");
        file.setToExtension("txt");

        when(conversionImageService.checkConversion(file)).thenReturn(false);
        when(conversionTxtService.checkConversion(file)).thenReturn(false);

        assertThatThrownBy(() -> changeConversionService.changeConversion(file))
                .isInstanceOf(ConversionException.class)
                .hasMessage("No support conversion from png to txt");
    }
}
