package com.example.demo.config;

import org.junit.jupiter.api.Test;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

class DeploymentProfileSmokeTest {

    @Test
    void verifyBasePropertiesConfigurations() throws IOException {
        Properties baseProps = new Properties();
        try (InputStream is = new FileInputStream("src/main/resources/application.properties")) {
            assertNotNull(is, "application.properties should exist in main resources");
            baseProps.load(is);
        }

        // Verify sensible environment variable fallbacks
        assertEquals("local-ai", baseProps.getProperty("spring.application.name"));
        assertEquals("${OLLAMA_BASE_URL:http://localhost:11434}", baseProps.getProperty("spring.ai.ollama.base-url"));
        assertEquals("${OLLAMA_MODEL:gemma4:e2b}", baseProps.getProperty("spring.ai.ollama.chat.model"));
        assertEquals("${OLLAMA_EMBEDDING_MODEL:gemma4:e2b}", baseProps.getProperty("spring.ai.ollama.embedding.model"));

        assertEquals("jdbc:postgresql://${DB_HOST:localhost}:${DB_PORT:5432}/${DB_NAME:local_ai}", baseProps.getProperty("spring.datasource.url"));
        assertEquals("${DB_USERNAME:postgres}", baseProps.getProperty("spring.datasource.username"));
        assertEquals("${DB_PASSWORD:postgrespassword}", baseProps.getProperty("spring.datasource.password"));
        
        assertEquals("5MB", baseProps.getProperty("spring.servlet.multipart.max-file-size"));
        assertEquals("${ALLOWED_CORS_ORIGIN:*}", baseProps.getProperty("app.security.cors.allowed-origins"));
    }

    @Test
    void verifyProductionProfilePropertiesConfigurations() throws IOException {
        Properties prodProps = new Properties();
        try (InputStream is = new FileInputStream("src/main/resources/application-prod.properties")) {
            assertNotNull(is, "application-prod.properties should exist in main resources");
            prodProps.load(is);
        }

        // Verify production connection mappings
        assertEquals("jdbc:postgresql://${DB_HOST:postgres}:${DB_PORT:5432}/${DB_NAME:local_ai}", prodProps.getProperty("spring.datasource.url"));
        assertEquals("${DB_USERNAME}", prodProps.getProperty("spring.datasource.username"));
        assertEquals("${DB_PASSWORD}", prodProps.getProperty("spring.datasource.password"));

        // Verify production-specific database hardening rules
        assertEquals("validate", prodProps.getProperty("spring.jpa.hibernate.ddl-auto"));
        assertEquals("false", prodProps.getProperty("spring.jpa.show-sql"));
        assertEquals("false", prodProps.getProperty("spring.ai.vectorstore.pgvector.initialize-schema"));
    }
}
