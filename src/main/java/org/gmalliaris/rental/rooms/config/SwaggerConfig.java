package org.gmalliaris.rental.rooms.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    @Bean
    public OpenAPI customOpenAPI() {
        return new OpenAPI()
                .info(new Info().title("RentalRooms API v2")
                        .description("Rental Rooms sample application")
                        .version("v0.0.1"))
                .components(new Components()
                        .addSecuritySchemes("BearerAccessToken",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("Bearer").bearerFormat("JWT"))
                        .addSecuritySchemes("BearerRefreshToken",
                                new SecurityScheme().type(SecurityScheme.Type.HTTP).scheme("Bearer").bearerFormat("JWT")));
    }
}