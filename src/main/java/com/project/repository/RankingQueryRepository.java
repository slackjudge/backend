package com.project.repository;

import com.project.dto.response.RankingRowResponse;
import com.project.entity.EurekaTeamName;
import com.project.entity.QProblemEntity;
import com.project.entity.QUserEntity;
import com.project.entity.QUsersProblemEntity;
import com.querydsl.core.types.Predicate;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.DateTimeExpression;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

/**
 * author : 박준희
 */
@Repository
@RequiredArgsConstructor
@Slf4j
public class RankingQueryRepository {

  private final JPAQueryFactory queryFactory;

    /**
     * 시작 구간 ~ 마감 기간과 그룹에 따른 데이터 집계
     * @param start
     * @param endInclusive
     * @param team
     * @return 총 점수를 인덱스로 하는 랭킹 리스트 반환
     */
  public List<RankingRowResponse> getRankingRows(LocalDateTime start, LocalDateTime endInclusive, EurekaTeamName team) {
    QUsersProblemEntity usersProblemEntity = QUsersProblemEntity.usersProblemEntity;
    QUserEntity userEntity = QUserEntity.userEntity;
    QProblemEntity problemEntity = QProblemEntity.problemEntity;

      DateTimeExpression<LocalDateTime> validAfterExpr =
              Expressions.dateTimeTemplate(
                      LocalDateTime.class,
                      "timestampadd(HOUR, 2, function('date_trunc', 'hour', {0}))",
                      userEntity.createdAt
              );

      // 쿼리 성능 확인
      long t0 = System.nanoTime();
      log.info("[랭킹 쿼리 시작] start={}, endInclusive={}, team={}", start, endInclusive, team);

      List<RankingRowResponse> result = queryFactory
              .select(Projections.constructor(RankingRowResponse.class,
                      userEntity.userId,
                      userEntity.username,
                      userEntity.bojTier,
                      problemEntity.problemLevel.sum().as("totalScore"),
                      problemEntity.problemLevel.count().as("solvedCount"),
                      userEntity.baekjoonId,
                      userEntity.teamName.stringValue()
              ))
              .from(usersProblemEntity)
              .join(usersProblemEntity.ref.user, userEntity)
              .join(usersProblemEntity.ref.problem, problemEntity)
              .where(
                      usersProblemEntity.solvedTime.gt(start),
                      usersProblemEntity.solvedTime.loe(endInclusive),
                      usersProblemEntity.solvedTime.goe(validAfterExpr),
                      teamFilter(team, userEntity)
              )
              .groupBy(userEntity.userId)
              .orderBy(
                      problemEntity.problemLevel.sum().desc(),
                      userEntity.username.asc()
              )
              .fetch();
      long time = (System.nanoTime() - t0) / 1_000_000;
      log.info("[랭킹 쿼리 종료]: time={}", time);

      return result;
  }

  private Predicate teamFilter(EurekaTeamName team, QUserEntity userEntity) {
    if (team == null) {
        return null;
    }
    return userEntity.teamName.eq(team);
  }
}
