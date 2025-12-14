package com.project.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.config.QuerydslConfig;
import com.project.config.TestContainerConfig;
import com.project.dto.response.MyPageResponse;
import com.project.entity.UserEntity;
import com.project.entity.ProblemEntity;
import com.project.entity.UsersProblemEntity;
import com.project.entity.UserRole;
import com.project.entity.EurekaTeamName;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;
import org.springframework.data.jpa.repository.config.EnableJpaAuditing;

@DataJpaTest
@EnableJpaAuditing
@Import({QuerydslConfig.class, TestContainerConfig.class, MyPageRepository.class})
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
class MyPageRepositoryTest {

  @Autowired MyPageRepository myPageRepository;
  @Autowired EntityManager em;

  private UserEntity user;

  @BeforeEach
  void setUp() {
    // 1. 테스트용 메인 유저 생성
      user =
              UserEntity.builder()
                      .slackId("123")
                      .baekjoonId("gkdud0909")
                      .username("최하영")
                      .totalSolvedCount(10)
                      .bojTier(14)
                      .isAlertAgreed(true)
                      .isDeleted(false)
                      .userRole(UserRole.USER)
                      .teamName(EurekaTeamName.BACKEND_FACE)
                      .build();
      em.persist(user);

    // 2. 문제 더미 데이터 생성
    ProblemEntity p1 = createProblem(1001, "별찍기", 5);
    ProblemEntity p2 = createProblem(1002, "A+B", 10);

    // 3. 풀이 기록 생성( 시나리오: 12월 5일에 2문제 해결)
    // 정렬 테스트 오전 10시 오후 2시
    createSolvedHistory(user, p1, LocalDate.of(2025, 12, 5).atTime(14, 0));
    createSolvedHistory(user, p2, LocalDate.of(2025, 12, 5).atTime(10, 0));

    // 4. 영속성 컨텍스트 초기화 ( 실제로 db에 나가도록)
    em.flush();
    em.clear();
  }

  @Test
  @DisplayName("월간 잔디 조회: 날짜별로 그룹핑되어 풀이 수가 정확히 나와야한다. ")
  void findGrassListTest() {
    // given
    int year = 2025;
    int month = 12;

    // when
    List<MyPageResponse.Grass> result =
        myPageRepository.findGrassList(user.getUserId(), year, month);

    // then
    assertThat(result).hasSize(1); // 5일 하루 -> 1개
    assertThat(result.get(0).getDate()).isEqualTo("2025-12-05");
    assertThat(result.get(0).getSolvedCount()).isEqualTo(2);
  }

  @Test
  @DisplayName("특정 날짜 풀이 문제 조회: 시간 오름차순으로 조회")
  void findSolvedProblemsByDateTest() {
      //given
      LocalDate date = LocalDate.of(2025, 12, 5);

      //when
      List<MyPageResponse.Problem> result =
              myPageRepository.findSolvedProblemsByDate(user.getUserId(), date);

      //then
      assertThat(result).hasSize(2);

      //정렬
      assertThat(result.get(0).getTitle()).isEqualTo("A+B");
      assertThat(result.get(0).getTierLevel()).isEqualTo(10);
      assertThat(result.get(1).getTitle()).isEqualTo("별찍기");
      assertThat(result.get(1).getTierLevel()).isEqualTo(5);
  }
  private ProblemEntity createProblem(int id, String title, int level) {
    ProblemEntity p =
        ProblemEntity.builder()
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
        UsersProblemEntity.builder().user(u).problem(p).isSolved(true).solvedTime(time).build());
  }
}
