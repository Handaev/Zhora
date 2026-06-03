package com.example.Zhora.service.impl;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.service.ChangeConversionService;
import com.example.Zhora.service.ConversionService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.nio.file.NoSuchFileException;
import java.util.List;

@Service
@RequiredArgsConstructor
public class ChangeConversionServiceImpl implements ChangeConversionService{

    private final List<ConversionService> conversionService;

    @Override
    public ConversionService changeConversion(FileConversionInbox fileConversion) throws ConversionException {
        String fromExtension = fileConversion.getName().split("\\.")[1];
        String toExtension = fileConversion.getToExtension();
        try {
            for (ConversionService conversionService : conversionService) {
                if (conversionService.checkConversion(fileConversion)) {
                    return conversionService;
                }
            }
        } catch (NoSuchFileException e){
            throw new ConversionException(String.format("No found file: %s", fileConversion.getName()));
        }

        throw new ConversionException(String.format("No support conversion from %s to %s", fromExtension, toExtension));
    }
}
