package com.ycr.framework.data.permission.integration;

import org.testcontainers.containers.JdbcDatabaseContainer;
import org.testcontainers.containers.MySQLContainer;
import org.testcontainers.containers.PostgreSQLContainer;

final class SqlDialectContainer {

    private SqlDialectContainer() {
    }

    static JdbcDatabaseContainer<?> create() {
        String dialect = requiredEnvironmentVariable("YCR_TEST_DATABASE");
        return switch (dialect) {
            case "mysql" -> new MySQLContainer<>("mysql:8.4")
                    .withDatabaseName("ycr")
                    .withUsername("ycr")
                    .withPassword("ycr");
            case "postgresql" -> new PostgreSQLContainer<>("postgres:16-alpine")
                    .withDatabaseName("ycr")
                    .withUsername("ycr")
                    .withPassword("ycr");
            default -> throw new IllegalArgumentException(
                    "Unsupported YCR_TEST_DATABASE: " + dialect + "; expected mysql or postgresql");
        };
    }

    private static String requiredEnvironmentVariable(String name) {
        String value = System.getenv(name);
        if (value == null || value.isBlank()) {
            throw new IllegalStateException(name + " is required when dialect integration tests are enabled");
        }
        return value;
    }
}
