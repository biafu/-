package com.zhinengpt.campuscatering.common.config;

import javax.sql.DataSource;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.datasource.init.ResourceDatabasePopulator;
import org.springframework.stereotype.Component;

@Component
public class DatabaseSchemaBootstrap implements ApplicationRunner {

    private static final Logger log = LoggerFactory.getLogger(DatabaseSchemaBootstrap.class);

    private final DataSource dataSource;

    public DatabaseSchemaBootstrap(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    @Override
    public void run(ApplicationArguments args) {
        ClassPathResource schemaResource = new ClassPathResource("db/schema.sql");
        if (!schemaResource.exists()) {
            log.warn("Schema bootstrap skipped because db/schema.sql was not found");
            return;
        }

        ResourceDatabasePopulator populator = new ResourceDatabasePopulator();
        populator.setSqlScriptEncoding("UTF-8");
        populator.addScript(schemaResource);
        populator.execute(dataSource);

        log.info("Database schema bootstrap completed from db/schema.sql");
    }
}
