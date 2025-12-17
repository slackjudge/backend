package com.project.repository;

import com.project.config.QueryDslConfig;
import com.project.config.security.JpaAuditingConfig;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.EurekaTeamName;
import org.junit.jupiter.api.AfterAll;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.DynamicPropertyRegistry;
import org.springframework.test.context.DynamicPropertySource;
import org.testcontainers.containers.PostgreSQLContainer;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Locale;
import java.util.Objects;
import java.util.stream.Collectors;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * author : 박준희
 */
@DataJpaTest
@Import({QueryDslConfig.class, JpaAuditingConfig.class, RankingQueryRepository.class})
@ActiveProfiles("test")
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RankingQueryRepositoryTest {

    private static final PostgreSQLContainer<?> POSTGRES =
            new PostgreSQLContainer<>("postgres:17")
                    .withDatabaseName("testdb")
                    .withUsername("postgres")
                    .withPassword("postgres");

    static {
        POSTGRES.start();
    }

    @DynamicPropertySource
    static void overrideDatasourceProps(DynamicPropertyRegistry registry) {
        registry.add("spring.datasource.url", POSTGRES::getJdbcUrl);
        registry.add("spring.datasource.username", POSTGRES::getUsername);
        registry.add("spring.datasource.password", POSTGRES::getPassword);
        registry.add("spring.datasource.driver-class-name", POSTGRES::getDriverClassName);

        registry.add("spring.jpa.hibernate.ddl-auto", () -> "create-drop");
        registry.add("spring.jpa.properties.hibernate.dialect",
                () -> "org.hibernate.dialect.PostgreSQLDialect");
    }

    @AfterAll
    static void stopContainer() {
        POSTGRES.stop();
    }

    @Autowired
    RankingQueryRepository rankingQueryRepository;

    @Autowired
    JdbcTemplate jdbcTemplate;

       @BeforeEach
    void seedData() throws IOException {
        truncateAllTablesRestartIdentity();
        executeSqlScriptWithoutTruncate("/sql/insert-ranking-test-data.sql");
    }

    private void truncateAllTablesRestartIdentity() {
        List<String> tables = jdbcTemplate.queryForList(
                "SELECT tablename FROM pg_tables WHERE schemaname = 'public'",
                String.class
        );

        if (tables.isEmpty()) {
            return;
        }

        String joined = tables.stream()
                .map(t -> "\"" + t + "\"")
                .collect(Collectors.joining(", "));

        jdbcTemplate.execute("TRUNCATE TABLE " + joined + " RESTART IDENTITY CASCADE");
    }

    private void executeSqlScriptWithoutTruncate(String classpath) throws IOException {
        ClassPathResource resource = new ClassPathResource(classpath);
        String raw = new String(resource.getInputStream().readAllBytes(), StandardCharsets.UTF_8);

        String noBlockComments = raw.replaceAll("(?s)/\\*.*?\\*/", " ");

        StringBuilder sb = new StringBuilder();
        for (String line : noBlockComments.split("\\R")) {
            String trimmed = line.trim();
            if (trimmed.startsWith("--")) continue;
            sb.append(line).append('\n');
        }

        String cleaned = sb.toString();

        for (String stmt : cleaned.split(";")) {
            String s = stmt.trim();
            if (s.isEmpty()) continue;

            String upper = s.toUpperCase(Locale.ROOT);

            if (upper.startsWith("TRUNCATE")) continue;
            if (upper.contains("REFERENTIAL_INTEGRITY")) continue;
            if (upper.startsWith("SET FOREIGN_KEY_CHECKS")) continue;

            jdbcTemplate.execute(s);
        }
    }

    @Test
    @DisplayName("group = ALL(null), 2025-12 전체 기간 → 점수 desc + 이름 asc로 정렬")
    void getRankingRows_allGroup_basicOrderAndAggregation() {
        // given
        LocalDateTime start = LocalDate.of(2025, 12, 1).atStartOfDay();
        LocalDateTime endExclusive = LocalDate.of(2026, 1, 1).atStartOfDay();

        // when
        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(start, endExclusive, null);

        // then
        assertThat(rows).hasSize(3);

        RankingRowResponse first = rows.get(0);
        RankingRowResponse second = rows.get(1);
        RankingRowResponse third = rows.get(2);

        // 1위
        assertThat(first.getUserId()).isEqualTo(2L);
        assertThat(first.getName()).isEqualTo("프론트유저");
        assertThat(first.getTotalScore()).isEqualTo(35);
        assertThat((long) first.getSolvedCount()).isEqualTo(3L);
        assertThat(teamString(first)).isEqualTo("FRONTEND_FACE");

        // 2위
        assertThat(second.getUserId()).isEqualTo(1L);
        assertThat(second.getName()).isEqualTo("백엔드유저");
        assertThat(second.getTotalScore()).isEqualTo(25);
        assertThat((long) second.getSolvedCount()).isEqualTo(2L);
        assertThat(teamString(second)).isEqualTo("BACKEND_FACE");

        // 3위
        assertThat(third.getUserId()).isEqualTo(3L);
        assertThat(third.getName()).isEqualTo("비대면유저");
        assertThat(third.getTotalScore()).isEqualTo(15);
        assertThat((long) third.getSolvedCount()).isEqualTo(1L);
        assertThat(teamString(third)).isEqualTo("FRONTEND_NON_FACE");
    }

    @Test
    @DisplayName("group = BACKEND_FACE 인 경우 해당 팀 유저만 집계")
    void getRankingRows_filterByBackendFaceGroup() {
        // given
        LocalDateTime start = LocalDate.of(2025, 12, 1).atStartOfDay();
        LocalDateTime endExclusive = LocalDate.of(2026, 1, 1).atStartOfDay();

        // when
        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(start, endExclusive, EurekaTeamName.BACKEND_FACE);

        // then
        assertThat(rows).hasSize(1);

        RankingRowResponse backendRow = rows.get(0);
        assertThat(backendRow.getUserId()).isEqualTo(1L);
        assertThat(backendRow.getName()).isEqualTo("백엔드유저");
        assertThat(teamString(backendRow)).isEqualTo("BACKEND_FACE");
        assertThat(backendRow.getTotalScore()).isEqualTo(25);
        assertThat((long) backendRow.getSolvedCount()).isEqualTo(2L);
    }

    @Test
    @DisplayName("[start, endExclusive) 기간 필터가 올바르게 적용되어 endExclusive 시각 이후 데이터는 제외")
    void getRankingRows_periodFilter_exclusiveEnd() {
        // given
        LocalDateTime start = LocalDateTime.of(2025, 12, 11, 13, 0);
        LocalDateTime endExclusive = LocalDateTime.of(2025, 12, 11, 14, 0);

        // when
        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(start, endExclusive, null);

        // then
        assertThat(rows).hasSize(3);

        RankingRowResponse backendRow = findByName(rows, "백엔드유저");
        RankingRowResponse frontendRow = findByName(rows, "프론트유저");
        RankingRowResponse nonFaceRow = findByName(rows, "비대면유저");

        assertThat(backendRow.getTotalScore()).isEqualTo(25);
        assertThat((long) backendRow.getSolvedCount()).isEqualTo(2L);

        assertThat(frontendRow.getTotalScore()).isEqualTo(15);
        assertThat((long) frontendRow.getSolvedCount()).isEqualTo(2L);

        assertThat(nonFaceRow.getTotalScore()).isEqualTo(15);
        assertThat((long) nonFaceRow.getSolvedCount()).isEqualTo(1L);
    }

    private RankingRowResponse findByName(List<RankingRowResponse> rows, String name) {
        return rows.stream()
                .filter(r -> Objects.equals(r.getName(), name))
                .findFirst()
                .orElseThrow(() -> new AssertionError("랭킹 결과에서 유저를 찾을 수 없음: " + name));
    }

    private String teamString(RankingRowResponse row) {
        return String.valueOf(row.getTeam());
    }
}
