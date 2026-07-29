package com.felipe.commonplace.common;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        // Uso pessoal: a API só escuta em localhost. O dev server (:5173) e o app
        // empacotado (que carrega via file://, origem "null") batem aqui de origens
        // diferentes — liberar qualquer origem é simples e seguro nesse cenário.
        registry.addMapping("/api/**")
                .allowedOriginPatterns("*")
                .allowedMethods("GET", "POST", "PUT", "DELETE")
                // sem isso o navegador esconde o nome do arquivo na exportação
                .exposedHeaders("Content-Disposition");
    }
}
