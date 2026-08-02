package com.Ariadne.knowledge;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

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
    void contextLoads() {}

    @Test
    void knowledgeRecordsTableExistsWithExpectedColumns() {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_schema = 'knowledge' AND table_name = 'knowledge_records'", String.class);

        assertThat(columns).containsExactlyInAnyOrder(
                "id", "repository_id", "ingestion_job_id", "git_url", "ingestion_status", "received_at");
    }

    @Test
    void ingestionJobIdMustBeUnique() {

        String jobId = UUID.randomUUID().toString();
        jdbcTemplate.update(
                "INSERT INTO knowledge.knowledge_records (repository_id, ingestion_job_id, git_url, ingestion_status) " +
                        "VALUES (gen_random_uuid(), ?::uuid, 'https://x', 'COMPLETED')", jobId);

        assertThatThrownBy(() -> jdbcTemplate.update(
                "INSERT INTO knowledge.knowledge_records (repository_id, ingestion_job_id, git_url, ingestion_status) " +
                        "VALUES (gen_random_uuid(), ?::uuid, 'https://x', 'COMPLETED')", jobId))
                .isInstanceOf(DataIntegrityViolationException.class);
    }
}