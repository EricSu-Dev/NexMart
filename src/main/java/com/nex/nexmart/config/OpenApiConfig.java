package com.nex.nexmart.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OpenApiConfig {

	private static final String JWT_SCHEME = "JWT";

	@Bean
	public OpenAPI nexMartOpenAPI() {
		return new OpenAPI()
				.info(new Info()
						.title("NexMart API")
						.version("1.0.0")
						.description("NexMart e-commerce backend API"))
				.addSecurityItem(new SecurityRequirement().addList(JWT_SCHEME))
				.components(new Components().addSecuritySchemes(JWT_SCHEME,
						new SecurityScheme()
								.name(JWT_SCHEME)
								.type(SecurityScheme.Type.HTTP)
								.scheme("bearer")
								.bearerFormat("JWT")));
	}
}
