package com.Cynyun.campusevent;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.Cynyun.campusevent.mapper")
public class CampusEventApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampusEventApplication.class, args);
    }
}