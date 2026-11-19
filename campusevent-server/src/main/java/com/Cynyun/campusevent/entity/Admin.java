package com.Cynyun.campusevent.entity;

import lombok.Data;

@Data
public class Admin {
    private String adminId;
    private String name;
    private String passwordHash;
}