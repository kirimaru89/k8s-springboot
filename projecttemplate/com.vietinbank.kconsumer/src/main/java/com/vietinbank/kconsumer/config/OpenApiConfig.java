package com.vietinbank.kconsumer.config;

import io.swagger.v3.oas.models.Components;
import io.swagger.v3.oas.models.media.Content;
import io.swagger.v3.oas.models.media.MediaType;
import io.swagger.v3.oas.models.media.Schema;
import io.swagger.v3.oas.models.responses.ApiResponse;
import io.swagger.v3.oas.models.responses.ApiResponses;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.tags.Tag;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;

import io.swagger.v3.oas.annotations.enums.SecuritySchemeType;
import io.swagger.v3.oas.annotations.security.SecurityScheme;
import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;

import org.springdoc.core.customizers.OpenApiCustomizer;

import java.util.List;

@Configuration
@SecurityScheme(name = "bearerAuth", scheme = "bearer", type = SecuritySchemeType.HTTP, bearerFormat = "JWT")
public class OpenApiConfig {
    @Value("${api.info.title}")
    String title;

    @Value("${api.info.version}")
    String version;

    @Value("${api.info.description}")
    String description;

    public static final String COMMON_ERROR_RESPONSE_SCHEMA = "ErrorResponseDto";
    public static final String UNAUTHORIZED_ERROR_RESPONSE_SCHEMA = "UnauthorizedErrorResponseDto";
    public static final String FORBIDDEN_ERROR_RESPONSE_SCHEMA = "ForbiddenErrorResponseDto";

    public static final String CONTENT_TYPE = "application/json";

    @Bean
    public OpenAPI customOpenAPI() {
        Schema<?> commonErrorResponseSchema = new Schema<>()
                .type("object")
                .addProperty("code", new Schema<>().type("number"))
                .addProperty("message", new Schema<>().type("string"));

        Schema<?> unauthorizedErrorSchema = new Schema<>()
                .type("object")
                .addProperty("code", new Schema<>().type("number").example(HttpStatus.UNAUTHORIZED.value()))
                .addProperty("message", new Schema<>().type("string").example("Unauthorized"));

        Schema<?> forbiddenErrorSchema = new Schema<>()
                .type("object")
                .addProperty("code", new Schema<>().type("number").example(HttpStatus.FORBIDDEN.value()))
                .addProperty("message", new Schema<>().type("string").example("Forbidden"));

        return new OpenAPI()
                .components(
                        new Components()
                                .addSchemas(COMMON_ERROR_RESPONSE_SCHEMA, commonErrorResponseSchema)
                                .addSchemas(UNAUTHORIZED_ERROR_RESPONSE_SCHEMA, unauthorizedErrorSchema)
                                .addSchemas(FORBIDDEN_ERROR_RESPONSE_SCHEMA, forbiddenErrorSchema)
                )
                .info(new Info().title(title).version(version).description(description));
    }

    @Bean
    public OpenApiCustomizer globalErrorResponses() {
        return openApi -> {
            openApi.getPaths().forEach((path, pathItem) ->
                    pathItem.readOperations().forEach(operation -> {
                        ApiResponses responses = operation.getResponses();

                        // 400
                        responses.addApiResponse(String.valueOf(HttpStatus.BAD_REQUEST.value()), 
                            generateErrorResponse(HttpStatus.BAD_REQUEST.value(), "Bad request"));
                        // 500
                        responses.addApiResponse(String.valueOf(HttpStatus.INTERNAL_SERVER_ERROR.value()), 
                            generateErrorResponse(HttpStatus.INTERNAL_SERVER_ERROR.value(), "Internal server error"));

                        // thêm theo controller
                        if (path.startsWith("/api/v1/digital-documents/library/books")) {
                            // set tag
                            String bookTagName = "Book Controller";
                            openApi.addTagsItem(new Tag().name(bookTagName).description("Book Controller Management"));
                            operation.setTags(List.of(bookTagName));
                            // require jwt token
                            operation.addSecurityItem(new SecurityRequirement().addList("bearerAuth"));

                            // 401
                            responses.addApiResponse(String.valueOf(HttpStatus.UNAUTHORIZED.value()), 
                                generateErrorResponse(HttpStatus.UNAUTHORIZED.value(), "Unauthorized"));
                            // 404
                            responses.addApiResponse(String.valueOf(HttpStatus.NOT_FOUND.value()), 
                                generateErrorResponse(HttpStatus.NOT_FOUND.value(), "Not found"));
                        }
                        if (path.startsWith("/api/v1/digital-documents/library/auth")) {
                            String authTagName = "Auth Controller";
                            openApi.addTagsItem(new Tag().name(authTagName).description("Auth Controller Management"));
                            operation.setTags(List.of(authTagName));
                        }
                    })
            );
        };
    }

    private Schema<?> generateErrorResponseSchema(int statusCode, String message) {
        return new Schema<>()
                .type("object")
                .addProperty("code", new Schema<>().type("number").example(statusCode))
                .addProperty("message", new Schema<>().type("string").example(message));
    }

    private ApiResponse generateErrorResponse(int statusCode, String message) {
        return new ApiResponse()
                .description(message)
                .content(new Content().addMediaType(CONTENT_TYPE,
                        new MediaType().schema(
                                generateErrorResponseSchema(statusCode, message)
                        )));
    }
}
