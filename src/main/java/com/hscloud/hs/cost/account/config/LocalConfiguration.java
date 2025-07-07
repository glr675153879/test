package com.hscloud.hs.cost.account.config;

import com.pig4cloud.pigx.common.security.component.PermitAllUrlProperties;
import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.ExternalDocumentation;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.License;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

import javax.annotation.PostConstruct;

/**
 * Author:  Administrator
 * Date:  2024/9/9 18:59
 */
@Slf4j
@Configuration
@ConditionalOnProperty(value = "local", havingValue = "true")
public class LocalConfiguration {

    @Autowired
    private PermitAllUrlProperties permitAllUrl;
    @PostConstruct
    private void securityFilterChain() {
        permitAllUrl.getIgnoreUrls().add("/v3/**");
        permitAllUrl.getIgnoreUrls().add("/swagger-ui/**");
        permitAllUrl.getIgnoreUrls().add("/doc.html");
        permitAllUrl.getIgnoreUrls().add("/webjars/**");
    }

    @Primary
    @Bean
    public OpenAPI restfulOpenApis() {
        return  new OpenAPI()
                .info(new Info().title("Spring Boot 3.0 Restful Open API")
                        .description("The Open API").version("1.0")
                        .license(new License().name("Apache")))
                .externalDocs(new ExternalDocumentation()
                        .description("The Open API"))
                .addSecurityItem(new SecurityRequirement().addList("Authorization"))
                .components(new Components().addSecuritySchemes("Authorization",
                        new SecurityScheme().name("Authorization").type(SecurityScheme.Type.HTTP).scheme("bearer").bearerFormat("")));
    }
}
