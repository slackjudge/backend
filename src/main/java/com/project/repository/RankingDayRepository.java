package com.project.repository;

import com.project.entity.UserEntity;
import io.lettuce.core.dynamic.annotation.Param;
import java.time.LocalDateTime;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface RankingDayRepository extends JpaRepository<UserEntity, Long> {
  /**
   * 일간 랭킹 계산 (Native Query) 특정 날짜 범위 내에서 각 유저가 획득한 점수(난이도 합)를 계산하고, 내 점수보다 높은 유저의 수를 카운트하여 순위를
   * 산출합니다.
   *
   * @param myScore 내 일간 획득 점수
   * @param startOfDay 조회 시작 시간 (00:00:00)
   * @param endOfDay 조회 종료 시간 (23:59:59)
   * @return 내 등수 (나보다 점수 높은 사람 수 + 1)
   */
  @Query(
      value =
          """
        SELECT COUNT(*) + 1
        FROM (
            SELECT up.user_id, SUM(p.problem_level) as total_score
            FROM users_problem up
            JOIN problem p ON up.problem_id = p.problem_id
            WHERE up.solved_time BETWEEN :startOfDay AND :endOfDay
            AND up.is_solved = true
            GROUP BY up.user_id
        ) as daily_stats
        WHERE daily_stats.total_score > :myScore
    """,
      nativeQuery = true)
  long calculateDailyRank(
      @Param("myScore") int myScore,
      @Param("startOfDay") LocalDateTime startOfDay,
      @Param("endOfDay") LocalDateTime endOfDay);
}
