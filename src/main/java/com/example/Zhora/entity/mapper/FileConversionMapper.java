package com.example.Zhora.entity.mapper;

import com.example.Zhora.entity.FileConversion;
import com.example.Zhora.record.ConversionRequestRecord;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.MappingConstants;


@Mapper(componentModel = MappingConstants.ComponentModel.SPRING)
public interface FileConversionMapper {
    @Mapping(target = "name", source = "fileName")
    @Mapping(target = "id", ignore = true)
    @Mapping(target = "conversion", constant = "false")
    FileConversion toFileConversion(ConversionRequestRecord record);
}