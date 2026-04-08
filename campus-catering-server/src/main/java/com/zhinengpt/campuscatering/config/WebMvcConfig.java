package com.zhinengpt.campuscatering.config;

import com.zhinengpt.campuscatering.security.ApiSignatureInterceptor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebMvcConfig implements WebMvcConfigurer {

    private final ApiSignatureInterceptor apiSignatureInterceptor;
    private final String uploadDir;

    public WebMvcConfig(ApiSignatureInterceptor apiSignatureInterceptor,
                        @Value("${app.storage.upload-dir:uploads}") String uploadDir) {
        this.apiSignatureInterceptor = apiSignatureInterceptor;
        this.uploadDir = uploadDir;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(apiSignatureInterceptor).addPathPatterns("/api/**");
    }

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {
        String resourceLocation = "file:" + java.nio.file.Paths.get(uploadDir).toAbsolutePath().normalize() + "/";
        registry.addResourceHandler("/uploads/**").addResourceLocations(resourceLocation);
    }
}
