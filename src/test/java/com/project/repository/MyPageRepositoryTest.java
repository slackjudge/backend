package com.project.repository;

import com.project.config.TestContainerConfig;
import com.project.config.TestQueryDslConfig;
import com.project.dto.response.GrassResponse;
import com.project.dto.response.ProblemResponse;
import com.project.entity.EurekaTeamName;
import com.project.entity.ProblemEntity;
import com.project.entity.UserEntity;
import com.project.entity.UsersProblemEntity;
import com.project.entity.UserRole;
import jakarta.persistence.EntityManager;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@DataJpaTest
@Import({TestContainerConfig.class, TestQueryDslConfig.class, MyPageRepository.class})
class MyPageRepositoryTest {

    @Autowired
    private MyPageRepository myPageRepository;

    @Autowired
    private EntityManager em;

    private UserEntity user;

    @BeforeEach
    void setUp() {
        // 1. 유저 생성 (가입일: 12월 1일 13:58)
        // -> 배치 제외 시간: 14:00:00 ~ 14:59:59 /15:00 부터 유효함
        user = UserEntity.builder()
                .slackId("U12345")
                .baekjoonId("testUser")
                .username("테스터")
                .totalSolvedCount(0)
                .bojTier(10)
                .isAlertAgreed(true)
                .isDeleted(false)
                .userRole(UserRole.USER)
                .teamName(EurekaTeamName.BACKEND_FACE)
                .createdAt(LocalDateTime.of(2025, 12, 1, 13, 58, 0))
                .updatedAt(LocalDateTime.now())
                .build();
        em.persist(user);

        // 2. 문제 생성
        ProblemEntity p1 = createProblem(1001, "문제1", 5);
        ProblemEntity p2 = createProblem(1002, "문제2", 10);
        ProblemEntity p3 = createProblem(1003, "문제3", 15);

        //풀이기록 생성: 모두 12월 1일에 생성
        LocalDate solvedDate = LocalDate.of(2025, 12, 1);

        createSolvedHistory(user, p1, solvedDate.atTime(14, 0, 0));

        createSolvedHistory(user, p2, solvedDate.atTime(14, 0, 0));

        //포함되어야함
        createSolvedHistory(user, p3, solvedDate.atTime(15, 0, 0));

        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("일간 문제 상세 조회: 기준시간 (validAfter) 이전의 데이터는 제외된다.")
    void findSolvedProblemList_BatchTime() {
        // given
        LocalDate targetDate = LocalDate.of(2025, 12, 1);

        // Service 로직과 시뮬: 가입(13:58) -> 13시 기준 + 2시간 -> 15:00 부터 조회 가능
        LocalDateTime validAfter = LocalDateTime.of(2025, 12, 1, 15, 0, 0);

        // when
        List<ProblemResponse> result = myPageRepository.findSolvedProblemList(
                user.getUserId(),
                targetDate,
                validAfter
        );

        // then
        assertThat(result).hasSize(1);
    }

    @Test
    @DisplayName("월간 잔디 조회: 기준 시간 이전의 데이터 카운트에서 제외된다")
    void findGrassList_FilterBatchTime() {
        // given
        int year = 2025;
        int month = 12;

        LocalDateTime validAfter = LocalDateTime.of(2025, 12, 1, 15, 0, 0);

        // when
        List<GrassResponse> result = myPageRepository.findGrassList(
                user.getUserId(), year, month, validAfter
        );

        // then
        // 12월 1일 데이터 확인
        GrassResponse day1 = result.stream()
                .filter(g -> g.date().equals("2025-12-01"))
                .findFirst()
                .orElseThrow();

        // 총 3문제 중 1문제(14:00) 제외 -> 1개여야 함
        assertThat(day1.solvedCount()).isEqualTo(1);
    }

    // --- Helper Methods ---
    private ProblemEntity createProblem(Integer id, String title, Integer level) {
        ProblemEntity p = ProblemEntity.builder()
                .problemId(id)
                .problemTitle(title)
                .problemLevel(level)
                .problemUrl("http://boj.kr/" + id)
                .build();
        em.persist(p);
        return p;
    }

    private void createSolvedHistory(UserEntity user, ProblemEntity problem, LocalDateTime solvedTime) {
        UsersProblemEntity up = UsersProblemEntity.builder()
                .user(user)
                .problem(problem)
                .isSolved(true)
                .solvedTime(solvedTime)
                .build();
        em.persist(up);
    }
}