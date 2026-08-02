package com.Ariadne.project;

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
    void contextLoads() {

    }

    @Test
    void allExpectedTablesExistAfterMigration() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'project'", String.class);

        assertThat(tables).containsExactlyInAnyOrder(
                "organizations", "projects", "repositories", "members", "flyway_schema_history");
    }

    @Test
    void organizationNameUniquenessIsCaseInsensitive() {

        jdbcTemplate.update("INSERT INTO project.organizations (name) VALUES ('Acme')");

        assertThatThrownBy(() ->
                jdbcTemplate.update("INSERT INTO project.organizations (name) VALUES ('ACME')"))
                .isInstanceOf(DataIntegrityViolationException.class);
    }

    @Test
    void repositoryCascadeDeletesWithProject() {

        jdbcTemplate.update("INSERT INTO project.organizations (id, name) VALUES (gen_random_uuid(), 'CascadeCo')");
        String orgId = jdbcTemplate.queryForObject(
                "SELECT id::text FROM project.organizations WHERE name = 'CascadeCo'", String.class);
        jdbcTemplate.update("INSERT INTO project.projects (id, org_id, name) VALUES (gen_random_uuid(), ?::uuid, 'Proj')", orgId);
        String projectId = jdbcTemplate.queryForObject(
                "SELECT id::text FROM project.projects WHERE org_id = ?::uuid", String.class, orgId);
        jdbcTemplate.update("INSERT INTO project.repositories (id, project_id, git_url) VALUES (gen_random_uuid(), ?::uuid, 'https://x')", projectId);

        jdbcTemplate.update("DELETE FROM project.organizations WHERE id = ?::uuid", orgId);

        Integer remaining = jdbcTemplate.queryForObject(
                "SELECT count(*) FROM project.repositories WHERE project_id = ?::uuid", Integer.class, projectId);
        assertThat(remaining).isZero();
    }
}