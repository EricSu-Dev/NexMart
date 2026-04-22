package com.nex.nexmart.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class SwaggerConfig {

    private static final String SECURITY_SCHEME_NAME = "BearerAuth";

    @Bean
    public OpenAPI openAPI() {
        return new OpenAPI()
            .info(new Info()
                .title("NexMart API")
                .description("NexMart 电商系统接口文档")
                .version("1.0.0")
            )
		    //Swagger 页面右上角会出现一个 “Authorize”（一把小锁）按钮。你只需要登录一次，把 Token 粘贴进去，
		    // 之后在网页上测试的所有接口都会自动带上这个 Token。
            // 全局添加 JWT 认证按钮(告诉 Swagger：“请给文档里的每一个接口都挂上这把锁。”)
            .addSecurityItem(new SecurityRequirement().addList(SECURITY_SCHEME_NAME))
		    //在告诉 Swagger：“我们的系统支持一种叫 JWT 的钥匙，它的格式是放在 HTTP Header 里的 Bearer Token。”
            .components(new Components()
                .addSecuritySchemes(SECURITY_SCHEME_NAME,
                    new SecurityScheme()
                        .name(SECURITY_SCHEME_NAME)
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                )
            );
    }
}
