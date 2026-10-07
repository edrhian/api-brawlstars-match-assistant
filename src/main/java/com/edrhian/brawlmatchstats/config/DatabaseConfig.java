package com.edrhian.brawlmatchstats.config;

import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

/**
 * Creates the folder for the SQLite file (if it does not exist) and the connection to it.
 */
@Configuration
public class DatabaseConfig {

    @Bean
    public DataSource dataSource(
            @Value("${app.database.folder}") String folder,
            @Value("${app.database.file}") String file) throws IOException {

        // Does nothing if the folder already exists
        Path folderPath = Path.of(folder);
        Files.createDirectories(folderPath);

        HikariConfig config = new HikariConfig();
        config.setDriverClassName("org.sqlite.JDBC");
        config.setJdbcUrl("jdbc:sqlite:" + folderPath.resolve(file));

        // SQLite allows one writer at a time, so one connection avoids "database is locked" errors
        config.setMaximumPoolSize(1);

        return new HikariDataSource(config);
    }
}