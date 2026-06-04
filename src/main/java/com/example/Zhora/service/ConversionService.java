package com.example.Zhora.service;

import com.example.Zhora.entity.FileConversionInbox;
import com.example.Zhora.exception.ConversionException;
import com.example.Zhora.record.ConversionMultipartFile;

import java.io.IOException;
import java.io.InputStream;
import java.nio.file.NoSuchFileException;

public interface ConversionService {

    ConversionMultipartFile convert(FileConversionInbox fileConversion) throws ConversionException;

    byte[] parsing(InputStream inputStream) throws IOException;

    boolean checkConversion(FileConversionInbox file) ;

    boolean checkAllowedTypes(String from, String to);
}
