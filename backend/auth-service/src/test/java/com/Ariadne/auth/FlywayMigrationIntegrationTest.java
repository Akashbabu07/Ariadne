package com.Ariadne.auth;

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
	void contextLoads() {}

	@Test
	void allExpectedTablesExist() {
		List<String> tables = jdbcTemplate.queryForList(
				"SELECT table_name FROM information_schema.tables WHERE table_schema = 'auth'", String.class);

		assertThat(tables).containsExactlyInAnyOrder(
				"users", "roles", "permissions", "role_permissions", "user_roles",
				"refresh_tokens", "flyway_schema_history");
	}

	@Test
	void seedRolesWereActuallyInserted() {

		List<String> roles = jdbcTemplate.queryForList("SELECT name FROM auth.roles ORDER BY name", String.class);
		assertThat(roles).containsExactly("ADMIN", "ENGINEER", "LEAD");
	}

	@Test
	void userEmailUniquenessIsCaseInsensitive() {
		jdbcTemplate.update(
				"INSERT INTO auth.users (org_id, email, password_hash) VALUES (gen_random_uuid(), 'a@x.com', 'hash')");

		org.assertj.core.api.Assertions.assertThatThrownBy(() -> jdbcTemplate.update(
						"INSERT INTO auth.users (org_id, email, password_hash) VALUES (gen_random_uuid(), 'A@X.COM', 'hash')"))
				.isInstanceOf(org.springframework.dao.DataIntegrityViolationException.class);
	}
}