package com.example.Zhora.entity.mapper;

import com.example.Zhora.entity.FileConversionOutbox;
import com.example.Zhora.record.ConversionResponseRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;

@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileConversionOutboxMapper {
    @Mapping(target = "fileName", source = "name")
    ConversionResponseRecord toConversionResponseRecord(FileConversionOutbox file);
}
