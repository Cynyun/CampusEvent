package com.Cynyun.campusevent.auth;

import com.Cynyun.campusevent.enums.Role;

import java.io.Serializable;

public record LoginUser(Role role, String id, String name) implements Serializable {
}