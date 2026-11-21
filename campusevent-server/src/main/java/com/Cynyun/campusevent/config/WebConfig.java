package com.Cynyun.campusevent.config;

import com.Cynyun.campusevent.auth.AdminInterceptor;
import com.Cynyun.campusevent.auth.AuthInterceptor;
import com.Cynyun.campusevent.auth.StudentInterceptor;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final AuthInterceptor authInterceptor;
    private final AdminInterceptor adminInterceptor;
    private final StudentInterceptor studentInterceptor;

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(authInterceptor)
                .addPathPatterns("/api/**")
                .excludePathPatterns("/api/auth/login");
        registry.addInterceptor(adminInterceptor).addPathPatterns("/api/admin/**");
        registry.addInterceptor(studentInterceptor).addPathPatterns("/api/student/**");
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/api/**")
                // 本地开发用 Vite/静态端口；生产为 Vibe Hosting 子域名。
                // 用 originPatterns 兼容固定公网域名和本地任意端口，避免同源 POST 被 CORS 误判为 403。
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*",
                        "https://cynyun-campus.thevibehosting.com",
                        "https://*.thevibehosting.com"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}