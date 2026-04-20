package com.studify.config;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeIn;
import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.servers.Server;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.List;

@Configuration
@SecurityScheme(
        name = "bearerAuth",
        description = "JWT Authentication - Insira o token com o prefixo Bearer",
        scheme = "bearer",
        type = SecuritySchemeType.HTTP,
        bearerFormat = "JWT",
        in = SecuritySchemeIn.HEADER
)
public class SwaggerConfig {

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
                .info(new Info()
                        .title(" Studify API")
                        .description("""
                                ## API de Gerenciamento de Estudos
                                
                                Sistema completo para gerenciar sua rotina de estudos:
                                **Matérias**: Organize suas disciplinas
                                **Sessões de Estudo**: Registre e acompanhe suas sessões
                                **Metas**: Defina e monitore seus objetivos
                                **Tarefas**: Gerencie suas atividades
                                
                                ### Autenticação
                                Use o endpoint `/api/v1/auth/register` para criar uma conta e `/api/v1/auth/login` para obter o JWT.
                                Clique em **Authorize** e insira: `Bearer <seu_token>`
                                """)
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("Studify")
                                .email("contato@studify.com"))
                        .license(new License()
                                .name("MIT License")))
                .servers(List.of(
                        new Server().url("http://localhost:8080").description("Local"),
                        new Server().url("https://studify-api.onrender.com").description("Produção")
                ));
    }
}
