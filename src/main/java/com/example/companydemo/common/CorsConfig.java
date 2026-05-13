package com.example.companydemo.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * 全局 CORS 配置。
 *
 * 前后端分离项目中，前端工程一般运行在 5173 端口（Vite 默认），
 * 后端运行在 8080 端口。浏览器为了安全会拦截跨域请求，必须在后端
 * 显式声明允许哪些来源、哪些方法、哪些请求头可以跨域访问。
 *
 * 教学要点：
 *   1. addMapping("/**") 表示所有接口都允许跨域。
 *   2. allowedOriginPatterns 比 allowedOrigins 更灵活，能用通配符。
 *      生产环境务必收窄白名单，不能写成 "*"。
 *   3. allowCredentials(true) 表示允许携带 Cookie，需要和具体来源
 *      一起使用，不能与 allowedOrigins("*") 同时启用。
 */
@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOriginPatterns(
                        "http://localhost:*",
                        "http://127.0.0.1:*"
                )
                .allowedMethods("GET", "POST", "PUT", "DELETE", "OPTIONS")
                .allowedHeaders("*")
                .allowCredentials(true)
                .maxAge(3600);
    }
}
