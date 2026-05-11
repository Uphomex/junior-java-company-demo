package com.example.companydemo;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.example.companydemo.mapper")
@SpringBootApplication
public class CompanyDemoApplication {

    public static void main(String[] args) {
        SpringApplication.run(CompanyDemoApplication.class, args);
    }
}

