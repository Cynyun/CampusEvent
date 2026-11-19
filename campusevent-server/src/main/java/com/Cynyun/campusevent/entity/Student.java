package com.Cynyun.campusevent.entity;

import lombok.Data;

@Data
public class Student {
    private String studentId;
    private String name;
    private String passwordHash;
}