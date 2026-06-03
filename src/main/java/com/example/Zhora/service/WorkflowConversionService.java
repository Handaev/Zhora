package com.example.Zhora.service;

import com.example.Zhora.record.ConversionMultipartFile;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.Future;

public interface WorkflowConversionService {

    List<Future<ConversionMultipartFile>> workflowConvert() throws InterruptedException;

    void save(ConversionMultipartFile file) throws ExecutionException, InterruptedException, IOException;
}
