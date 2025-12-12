package com.project.repository;

import com.project.config.QuerydslConfig;
import com.project.config.TestContainerConfig;
import com.project.config.security.JpaAuditingConfig;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.EurekaTeamName;
import com.project.entity.ProblemEntity;
import com.project.entity.UserEntity;
import com.project.entity.UsersProblemEntity;
import jakarta.persistence.EntityManager;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({QuerydslConfig.class, TestContainerConfig.class, JpaAuditingConfig.class, RankingQueryRepository.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class RankingQueryRepositoryTest {

    @Autowired
    RankingQueryRepository rankingQueryRepository;

    @Autowired
    EntityManager em;

    private UserEntity backend1;   // 박준희
    private UserEntity backend2;   // 김도연
    private UserEntity frontend1;  // 최하영

    private ProblemEntity easy;    // 5점
    private ProblemEntity mid;     // 10점
    private ProblemEntity hard;    // 15점

    @BeforeEach
    void setUp() {
        // === 유저 생성 ===
        backend1 = UserEntity.builder()
                .slackId("s1")
                .baekjoonId("b1")
                .username("박준희")
                .bojTier(5)
                .teamName(EurekaTeamName.BACKEND_FACE)
                .totalSolvedCount(0)
                .build();

        backend2 = UserEntity.builder()
                .slackId("s2")
                .baekjoonId("b2")
                .username("최하영")
                .bojTier(20)
                .teamName(EurekaTeamName.BACKEND_FACE)
                .totalSolvedCount(0)
                .build();

        frontend1 = UserEntity.builder()
                .slackId("s3")
                .baekjoonId("b3")
                .username("김도연")
                .bojTier(30)
                .teamName(EurekaTeamName.FRONTEND_FACE)
                .totalSolvedCount(0)
                .build();

        em.persist(backend1);
        em.persist(backend2);
        em.persist(frontend1);

        // === 문제 생성 ===
        easy = ProblemEntity.builder()
                .problemId(1001)
                .problemTitle("A+B")
                .problemLevel(5)
                .problemUrl("url1")
                .build();

        mid = ProblemEntity.builder()
                .problemId(1002)
                .problemTitle("별찍기")
                .problemLevel(10)
                .problemUrl("url2")
                .build();

        hard = ProblemEntity.builder()
                .problemId(1003)
                .problemTitle("골드문제")
                .problemLevel(15)
                .problemUrl("url3")
                .build();

        em.persist(easy);
        em.persist(mid);
        em.persist(hard);

        // === 풀이 이력 생성 ===
        LocalDateTime base = LocalDate.of(2025, 12, 11).atTime(10, 0);

        // backend1: easy + mid → 5 + 10 = 15점, 2문제
        em.persist(UsersProblemEntity.builder()
                .user(backend1)
                .problem(easy)
                .isSolved(true)
                .solvedTime(base)
                .build());

        em.persist(UsersProblemEntity.builder()
                .user(backend1)
                .problem(mid)
                .isSolved(true)
                .solvedTime(base.plusHours(1))
                .build());

        // backend2: hard만 풂 → 15점, 1문제
        em.persist(UsersProblemEntity.builder()
                .user(backend2)
                .problem(hard)
                .isSolved(true)
                .solvedTime(base)
                .build());

        // frontend1: easy만 풂 → 5점, 1문제 (다른 팀)
        em.persist(UsersProblemEntity.builder()
                .user(frontend1)
                .problem(easy)
                .isSolved(true)
                .solvedTime(base)
                .build());

        // 기간 밖(11월 데이터) - 집계에서 제외되는지 확인용 2025-11-30T23:00:00 15점
        LocalDateTime outOfRange = LocalDate.of(2025, 11, 30).atTime(23, 0);
        em.persist(UsersProblemEntity.builder()
                .user(backend1)
                .problem(hard)
                .isSolved(true)
                .solvedTime(outOfRange)
                .build());

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("기간 + 그룹(팀) 조건으로 점수/풀이 수를 집계하고, 점수 desc + 이름 asc 로 정렬")
    void getRankingRows_basic_group_and_order() {
        // given: 2025-12 한 달 전체, 백엔드 대면반만
        LocalDateTime start = LocalDate.of(2025, 12, 1).atStartOfDay();
        LocalDateTime endExclusive = LocalDate.of(2026, 1, 1).atStartOfDay(); // [start, endExclusive)
        String group = "BACKEND_FACE";

        // when
        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(start, endExclusive, group);

        // then
        assertThat(rows).hasSize(2); // frontend1 는 다른 팀이라 제외

        RankingRowResponse first = rows.get(0);
        RankingRowResponse second = rows.get(1);

        // backend1: 15점(5+10), 2문제
        // backend2: 15점(15),   1문제
        // 점수 동점 → 이름 오름차순 → 박준희(backend1) 먼저
        assertThat(first.getUserId()).isEqualTo(backend1.getUserId());
        assertThat(first.getTotalScore()).isEqualTo(15);
        assertThat(first.getSolvedCount()).isEqualTo(2);

        assertThat(second.getUserId()).isEqualTo(backend2.getUserId());
        assertThat(second.getTotalScore()).isEqualTo(15);
        assertThat(second.getSolvedCount()).isEqualTo(1);
    }

    @Test
    @DisplayName("group = ALL 인 경우 모든 팀을 포함해 전체 랭킹을 조회한다")
    void getRankingRows_all_group() {
        // given
        LocalDateTime start = LocalDate.of(2025, 12, 1).atStartOfDay();
        LocalDateTime endExclusive = LocalDate.of(2026, 1, 1).atStartOfDay();

        // when
        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(start, endExclusive, "ALL");

        // then
        assertThat(rows).hasSize(3);
        assertThat(rows)
                .extracting(RankingRowResponse::getUserId)
                .containsExactlyInAnyOrder(
                        backend1.getUserId(),
                        backend2.getUserId(),
                        frontend1.getUserId()
                );
    }

    @Test
    @DisplayName("group != ALL 일 때 해당 팀만 조회된다")
    void getRankingRows_filterByGroup() {
        // given
        LocalDateTime start = LocalDate.of(2025, 12, 11).atStartOfDay();
        LocalDateTime endExclusive = start.plusDays(1);
        String group = "BACKEND_FACE";

        // when
        List<RankingRowResponse> results =
                rankingQueryRepository.getRankingRows(start, endExclusive, group);

        // then
        assertThat(results).isNotEmpty(); // 최소 backend1, backend2 두 명
        assertThat(results)
                .allMatch(r -> r.getTeam().equals("BACKEND_FACE"));
        assertThat(results).hasSize(2);
        assertThat(results)
                .extracting(RankingRowResponse::getUserId)
                .containsExactlyInAnyOrder(
                        backend1.getUserId(),
                        backend2.getUserId()
                );
    }


    @Test
    @DisplayName("[start, endExclusive) 기간 조건에 맞지 않는 solvedTime 은 집계되지 않는다")
    void getRankingRows_period_filter() {
        // given: 12월 11일 하루만 포함되도록
        LocalDateTime start = LocalDate.of(2025, 12, 11).atStartOfDay();
        LocalDateTime endExclusive = LocalDate.of(2025, 12, 12).atStartOfDay();

        // when
        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(start, endExclusive, "BACKEND_FACE");

        // then
        // outOfRange(11월 30일) 에 넣어둔 hard는 포함되지 않아야 함
        // → backend1 점수는 5+10 = 15 그대로
        RankingRowResponse backend1Row = rows.stream()
                .filter(r -> r.getUserId().equals(backend1.getUserId()))
                .findFirst()
                .orElseThrow();

        assertThat(backend1Row.getTotalScore()).isEqualTo(15);
        assertThat(backend1Row.getSolvedCount()).isEqualTo(2);
    }
}
