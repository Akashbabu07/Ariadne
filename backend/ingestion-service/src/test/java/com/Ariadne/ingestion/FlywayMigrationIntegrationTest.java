package com.Ariadne.ingestion;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@SpringBootTest
@Testcontainers
class FlywayMigrationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("atlas_test").withUsername("atlas").withPassword("atlas_dev_password");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {
    }

    @Test
    void ingestionJobsTableExistsWithExpectedColumns() {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_schema = 'ingestion' AND table_name = 'ingestion_jobs'", String.class);

        assertThat(columns).containsExactlyInAnyOrder(
                "id", "repository_id", "git_url", "status", "error_message", "created_at", "updated_at");
    }

    @Test
    void statusDefaultsToQueuedWhenNotSpecified() {
        jdbcTemplate.update(
                "INSERT INTO ingestion.ingestion_jobs (repository_id, git_url) VALUES (gen_random_uuid(), 'https://x')");

        String status = jdbcTemplate.queryForObject(
                "SELECT status FROM ingestion.ingestion_jobs LIMIT 1", String.class);
        assertThat(status).isEqualTo("QUEUED");
    }
}