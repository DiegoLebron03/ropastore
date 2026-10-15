package com.ropastore;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.net.URI;
import java.util.HashMap;
import java.util.Map;

@SpringBootApplication
public class RopaStoreApplication {

    public static void main(String[] args) {
        loadNeonEnvironment();
        SpringApplication.run(RopaStoreApplication.class, args);
    }

    private static void loadNeonEnvironment() {
        File envFile = new File(".env.local");
        if (!envFile.exists()) {
            envFile = new File(".env");
        }
        Map<String, String> envMap = new HashMap<>();
        if (envFile.exists()) {
            try (BufferedReader reader = new BufferedReader(new FileReader(envFile))) {
                String line;
                while ((line = reader.readLine()) != null) {
                    line = line.trim();
                    if (line.isEmpty() || line.startsWith("#") || !line.contains("=")) {
                        continue;
                    }
                    int eqIndex = line.indexOf('=');
                    String key = line.substring(0, eqIndex).trim();
                    String val = line.substring(eqIndex + 1).trim();
                    if ((val.startsWith("\"") && val.endsWith("\"")) || (val.startsWith("'") && val.endsWith("'"))) {
                        val = val.substring(1, val.length() - 1);
                    }
                    envMap.put(key, val);
                }
            } catch (Exception e) {
                System.err.println("Could not load env file: " + e.getMessage());
            }
        }

        String dbUrl = System.getenv("DATABASE_URL");
        if (dbUrl == null || dbUrl.isBlank()) {
            dbUrl = envMap.get("DATABASE_URL");
        }

        String unpooledUrl = System.getenv("DATABASE_URL_UNPOOLED");
        if (unpooledUrl == null || unpooledUrl.isBlank()) {
            unpooledUrl = envMap.get("DATABASE_URL_UNPOOLED");
        }

        if (dbUrl != null && !dbUrl.isBlank()) {
            try {
                URI uri = new URI(dbUrl.startsWith("jdbc:") ? dbUrl.substring(5) : dbUrl);
                String userInfo = uri.getUserInfo();
                String username = null;
                String password = null;
                if (userInfo != null && userInfo.contains(":")) {
                    String[] parts = userInfo.split(":", 2);
                    username = parts[0];
                    password = parts[1];
                }

                String host = uri.getHost();
                int port = uri.getPort() > 0 ? uri.getPort() : 5432;
                String path = uri.getPath();

                String jdbcUrl = "jdbc:postgresql://" + host + ":" + port + path + "?sslmode=require";

                System.setProperty("spring.datasource.url", jdbcUrl);
                if (username != null) {
                    System.setProperty("spring.datasource.username", username);
                }
                if (password != null) {
                    System.setProperty("spring.datasource.password", password);
                }
                System.setProperty("spring.datasource.driver-class-name", "org.postgresql.Driver");
                System.setProperty("spring.jpa.database-platform", "org.hibernate.dialect.PostgreSQLDialect");
                System.setProperty("spring.jpa.hibernate.ddl-auto", "update");

                // Configurar Flyway usando la conexión directa (unpooled) recomendada por Neon
                if (unpooledUrl != null && !unpooledUrl.isBlank()) {
                    URI unpooledUri = new URI(unpooledUrl.startsWith("jdbc:") ? unpooledUrl.substring(5) : unpooledUrl);
                    String unpooledHost = unpooledUri.getHost();
                    int unpooledPort = unpooledUri.getPort() > 0 ? unpooledUri.getPort() : 5432;
                    String unpooledPath = unpooledUri.getPath();
                    String unpooledJdbc = "jdbc:postgresql://" + unpooledHost + ":" + unpooledPort + unpooledPath + "?sslmode=require";
                    System.setProperty("spring.flyway.url", unpooledJdbc);
                    if (username != null) {
                        System.setProperty("spring.flyway.user", username);
                    }
                    if (password != null) {
                        System.setProperty("spring.flyway.password", password);
                    }
                }

                System.setProperty("spring.flyway.enabled", "true");
                System.setProperty("spring.flyway.locations", "classpath:db/migration");
                System.setProperty("spring.flyway.baseline-on-migrate", "true");

                System.out.println("=================================================");
                System.out.println(" Neon Postgres configurado con éxito:");
                System.out.println(" • App datasource (pooled): " + host);
                if (unpooledUrl != null) {
                    System.out.println(" • Flyway migrations (unpooled): direct endpoint");
                }
                System.out.println("=================================================");
            } catch (Exception e) {
                System.err.println("Error parsing DATABASE_URL: " + e.getMessage());
            }
        }
    }
}
