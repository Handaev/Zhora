package com.example.Zhora.controller;


import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/conversion")
public class ConversionController {

    @GetMapping("/test")
    public String test(){
        return "conversion test";
    }
}
