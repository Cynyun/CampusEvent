package com.Cynyun.campusevent.controller;

import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

/**
 * 前端 dist 已放入 classpath:/static/，这里为 Vue Router 的 history 路由做回退。
 * API 路径以 /api 开头，不会被这些页面路由匹配。
 */
@Controller
public class SpaController {

    @GetMapping({
            "/",
            "/login",
            "/activities",
            "/activities/**",
            "/my-registrations",
            "/messages",
            "/admin",
            "/admin/**"
    })
    public String index() {
        return "forward:/index.html";
    }
}
