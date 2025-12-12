package com.project.repository;

import static org.assertj.core.api.Assertions.assertThat;

import com.project.config.TestContainerConfig;
import com.project.entity.ProblemEntity;
import com.project.entity.UserEntity;
import com.project.entity.UsersProblemEntity;
import jakarta.persistence.EntityManager;
import java.time.LocalDate;
import java.time.LocalDateTime;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.jdbc.AutoConfigureTestDatabase;
import org.springframework.boot.test.autoconfigure.orm.jpa.DataJpaTest;
import org.springframework.context.annotation.Import;

@DataJpaTest
@Import(TestContainerConfig.class)
@AutoConfigureTestDatabase(replace = AutoConfigureTestDatabase.Replace.NONE)
public class RankingDayRepositoryTest {
  @Autowired RankingDayRepository rankingDayRepository;
  @Autowired EntityManager em;

  @Test
  @DisplayName("일간 랭킹: 나보다 점수 높은 사람이 1명이면 내 등수는 2등이어야 한다.")
  void calculateDailyRankTest() {
    // given
    UserEntity me = createUser("최하영");
    UserEntity otherHigh = createUser("고수야");
    UserEntity otherLow = createUser("하수야");
    // 문제 생성
    ProblemEntity p10 = createProblem(10);
    ProblemEntity p20 = createProblem(20);
    ProblemEntity p5 = createProblem(5);

    // 문제 풀이 ( 오늘 날짜)
    LocalDateTime today = LocalDate.now().atTime(12, 0);

    // 나 - 레벨 10
    solve(me, p10, today);
    // 고수 - 20
    solve(otherHigh, p20, today);
    // 하수 - 5
    solve(otherLow, p5, today);

    // db 반영
    em.flush();
    em.clear();

    // when
    // 나의 점수는 10점이라고 가정하고 랭킹 조회
    int myScore = 10;
    LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
    LocalDateTime endOfDay = LocalDate.now().atTime(23, 59, 59);

    long rank = rankingDayRepository.calculateDailyRank(myScore, startOfDay, endOfDay);

    // then
    assertThat(rank).isEqualTo(2);
  }

  private UserEntity createUser(String name) {
    UserEntity u =
        UserEntity.builder()
            .slackId(name)
            .baekjoonId(name)
            .username(name)
            .totalSolvedCount(0)
            .build();
    em.persist(u);
    return u;
  }

  private ProblemEntity createProblem(int level) {
    ProblemEntity p =
        ProblemEntity.builder()
            .problemId(1000 + level)
            .problemTitle("Level " + level)
            .problemLevel(level)
            .problemUrl("url")
            .build();
    em.persist(p);
    return p;
  }

  private void solve(UserEntity u, ProblemEntity p, LocalDateTime time) {
    em.persist(
        UsersProblemEntity.builder().user(u).problem(p).isSolved(true).solvedTime(time).build());
  }
}
