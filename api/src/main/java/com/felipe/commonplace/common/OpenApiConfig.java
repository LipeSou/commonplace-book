package com.felipe.commonplace.common;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

    @Bean
    public OpenAPI commonplaceOpenAPI() {
        return new OpenAPI().info(new Info()
                .title("Commonplace Book API")
                .version("v1")
                .description("""
                        API do Commonplace Book — coletar, conectar e recuperar conhecimento em markdown.

                        Princípio: o `content` bruto de cada nota é a fonte da verdade, imutável e \
                        preservado byte a byte. Tudo o mais (tags, links, busca) é derivado dele \
                        — índice descartável e recalculável."""));
    }
}
