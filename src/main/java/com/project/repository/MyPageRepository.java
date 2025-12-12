package com.project.repository;

import static com.project.entity.QProblemEntity.problemEntity;
import static com.project.entity.QUsersProblemEntity.usersProblemEntity;

import com.project.dto.response.MyPageResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MyPageRepository {

  private final JPAQueryFactory queryFactory;

  /**
   * 월간 잔디 데이터 조회(날짜별 풀이 수 집계) TO_CHAR함수를 사용해 날짜를 포맷팅하고 그룹핑합니다.
   *
   * @param userId 조회할 사용자 ID
   * @param year 조회할 연도
   * @param month 조회할 월
   * @return 날짜(YYYY-MM-DD)와 해당 날짜의 풀이 수가 담긴 DTO 리스트
   */
  public List<MyPageResponse.Grass> findGrassList(Long userId, int year, int month) {
    // 해당 월의 시작과 끝 계산
    LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
    LocalDateTime endOfMonth =
        LocalDate.of(year, month, 1).plusMonths(1).minusDays(1).atTime(23, 59, 59);

    // 날짜 포매팅: TO_CHAR(solved_time, 'YYYY-MM-DD')
    StringExpression dateStr =
        Expressions.stringTemplate("TO_CHAR({0}, 'YYYY-MM-DD')", usersProblemEntity.solvedTime)
            .stringValue();

    return queryFactory
        .select(
            Projections.constructor(
                MyPageResponse.Grass.class, dateStr, usersProblemEntity.count().intValue()))
        .from(usersProblemEntity)
        .where(
            usersProblemEntity.user.userId.eq(userId),
            usersProblemEntity.isSolved.isTrue(),
            usersProblemEntity.solvedTime.between(startOfMonth, endOfMonth))
        .groupBy(dateStr)
        .fetch();
  }

  /**
   * 특정 날짜에 푼 문제 상세 목록 조회 푼 시간대로 정렬해 반환합니다.
   *
   * @param userId 조회할 사용자 ID
   * @param date 조회할 날짜 (YYYY-MM-DD)
   * @return 해당 날짜(YYYY-MM-DD)에 푼 문제 정보 ( 제목, 난이도, 링크) 리스트
   */
  // 날짜별 푼 문제
  public List<MyPageResponse.Problem> findSolvedProblemsByDate(Long userId, LocalDate date) {
    LocalDateTime startOfDay = date.atStartOfDay();
    LocalDateTime endOfDay = date.atTime(23, 59, 59);

    return queryFactory
        .select(
            Projections.constructor(
                MyPageResponse.Problem.class,
                problemEntity.problemTitle,
                problemEntity.problemLevel,
                problemEntity.problemUrl))
        .from(usersProblemEntity)
        .join(usersProblemEntity.problem, problemEntity)
        .where(
            usersProblemEntity.user.userId.eq(userId),
            usersProblemEntity.isSolved.isTrue(),
            usersProblemEntity.solvedTime.between(startOfDay, endOfDay))
        .orderBy(usersProblemEntity.solvedTime.asc()) // 난이도 높은 순 정렬
        .fetch();
  }
}
