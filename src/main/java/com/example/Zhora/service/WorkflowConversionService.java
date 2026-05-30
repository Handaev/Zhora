package com.example.Zhora.service;

import org.springframework.web.multipart.MultipartFile;

import java.util.List;
import java.util.concurrent.Future;

public interface WorkflowConversionService {

    List<Future<MultipartFile>> workflowConvert();

    void workflowSave(List<Future<MultipartFile>> files);
}
