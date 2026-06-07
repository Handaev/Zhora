package com.example.Zhora;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;

@EnableDiscoveryClient
@SpringBootApplication
public class ZhoraApplication {

	public static void main(String[] args) {
		SpringApplication.run(ZhoraApplication.class, args);
	}

}
