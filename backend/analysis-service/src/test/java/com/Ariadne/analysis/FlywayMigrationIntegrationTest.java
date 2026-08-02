package com.Ariadne.analysis;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.testcontainers.service.connection.ServiceConnection;
import org.springframework.jdbc.core.JdbcTemplate;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;


@SpringBootTest
@Testcontainers
class FlywayMigrationIntegrationTest {

    @Container
    @ServiceConnection
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:16-alpine")
            .withDatabaseName("atlas_test")
            .withUsername("atlas")
            .withPassword("atlas_dev_password");

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void contextLoads() {

    }

    @Test
    void allExpectedTablesExistAfterMigration() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT table_name FROM information_schema.tables WHERE table_schema = 'analysis'",
                String.class);

        assertThat(tables).containsExactlyInAnyOrder(
                "analysis_reports",
                "impact_analysis_reports",
                "file_metric_snapshots",
                "drift_reports",
                "flyway_schema_history"
        );
    }

    @Test
    void flywayAppliedEveryMigrationSuccessfully() {
        List<Map<String, Object>> applied = jdbcTemplate.queryForList(
                "SELECT version, success FROM analysis.flyway_schema_history " +
                        "WHERE version IS NOT NULL ORDER BY installed_rank");

        assertThat(applied).hasSize(2);
        assertThat(applied).allSatisfy(row -> assertThat(row.get("success")).isEqualTo(true));
        assertThat(applied.stream().map(row -> row.get("version")).toList())
                .containsExactly("1", "2");
    }

    @Test
    void impactAnalysisReportsTableHasExpectedColumns() {
        List<String> columns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_schema = 'analysis' AND table_name = 'impact_analysis_reports'",
                String.class);

        assertThat(columns).containsExactlyInAnyOrder(
                "id", "repository_id", "target_path", "impacted_files", "explanation", "created_at");
    }

    @Test
    void driftReportsAndFileMetricSnapshotsTablesHaveExpectedColumns() {
        List<String> driftColumns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_schema = 'analysis' AND table_name = 'drift_reports'",
                String.class);
        assertThat(driftColumns).containsExactlyInAnyOrder(
                "id", "repository_id", "drifted_files", "explanation", "created_at");

        List<String> snapshotColumns = jdbcTemplate.queryForList(
                "SELECT column_name FROM information_schema.columns " +
                        "WHERE table_schema = 'analysis' AND table_name = 'file_metric_snapshots'",
                String.class);
        assertThat(snapshotColumns).containsExactlyInAnyOrder(
                "id", "repository_id", "file_path", "fan_in", "fan_out", "captured_at");
    }
}