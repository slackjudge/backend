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
        // 1. 테스트용 메인 유저 생성
        user = UserEntity.builder()
                .slackId("123")
                .baekjoonId("gkdud0909")
                .username("최하영")
                .totalSolvedCount(10)
                .bojTier(14)
                .isAlertAgreed(true)
                .isDeleted(false)
                .userRole(UserRole.USER)
                .teamName(EurekaTeamName.BACKEND_FACE)
                // [핵심] 빌더 사용 시 테스트 환경에서는 날짜가 자동 주입 안 될 수 있으므로 명시
                .createdAt(LocalDateTime.now())
                .updatedAt(LocalDateTime.now())
                .build();
        em.persist(user);

        // 2. 문제 더미 데이터 생성
        ProblemEntity p1 = createProblem(1001, "별찍기", 5);
        ProblemEntity p2 = createProblem(1002, "A+B", 10);

        // 3. 풀이 기록 생성 (시나리오: 12월 5일에 2문제 해결)
        // p2(A+B)는 오전 10시, p1(별찍기)는 오후 2시 -> 오름차순 정렬 시 p2가 먼저 나와야 함
        createSolvedHistory(user, p1, LocalDate.of(2025, 12, 5).atTime(14, 0));
        createSolvedHistory(user, p2, LocalDate.of(2025, 12, 5).atTime(10, 0));

        // 4. 영속성 컨텍스트 초기화 (쿼리가 실제로 DB로 날아가도록)
        em.flush();
        em.clear();
    }

    @Test
    @DisplayName("월간 잔디 조회: 날짜별로 그룹핑되어 풀이 수가 정확히 나와야한다.")
    void findGrassListTest() {
        // given
        int year = 2025;
        int month = 12;

        // when
        List<GrassResponse> result =
                myPageRepository.findGrassList(user.getUserId(), year, month);

        // then
        assertThat(result).hasSize(1); // 5일 하루 -> 1개 데이터

        GrassResponse dayData = result.get(0);
        assertThat(dayData.date()).isEqualTo("2025-12-05");
        assertThat(dayData.solvedCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("일간 문제 상세 조회: 해당 날짜의 solved=true만 오름차순으로 조회되어야 한다.")
    void findSolvedProblemListTest() {
        // given
        LocalDate date = LocalDate.of(2025, 12, 5);

        // when
        List<ProblemResponse> result =
                myPageRepository.findSolvedProblemList(user.getUserId(), date);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).title()).isEqualTo("A+B");   // 10:00
        assertThat(result.get(1).title()).isEqualTo("별찍기"); // 14:00
    }



    private ProblemEntity createProblem(int id, String title, int level) {
        ProblemEntity p = ProblemEntity.builder()
                .problemId(id)
                .problemTitle(title)
                .problemLevel(level)
                .problemUrl("url")
                .build();
        em.persist(p);
        return p;
    }

    private void createSolvedHistory(UserEntity u, ProblemEntity p, LocalDateTime time) {
        em.persist(
                UsersProblemEntity.builder()
                        .user(u)
                        .problem(p)
                        .isSolved(true)
                        .solvedTime(time)
                        .build()
        );
    }
}

