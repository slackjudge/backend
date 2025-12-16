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
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
@RequiredArgsConstructor
public class RankingQueryRepository {

  private final JPAQueryFactory queryFactory;

    /**
     * 기간, 그룹, 페이징 조건으로 랭킹 조회
     */
  public List<RankingRowResponse> getRankingRows(LocalDateTime start, LocalDateTime endExclusive, EurekaTeamName team) {
    QUsersProblemEntity usersProblemEntity = QUsersProblemEntity.usersProblemEntity;
    QUserEntity userEntity = QUserEntity.userEntity;
    QProblemEntity problemEntity = QProblemEntity.problemEntity;

    //valid 시점 결정
      DateTimeExpression<LocalDateTime> validAfterExpr =
              Expressions.dateTimeTemplate(
                      LocalDateTime.class,
                      "date_trunc('hour', {0}) + interval '2 hour'",
                      userEntity.createdAt
              );

   return queryFactory
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
                    usersProblemEntity.solvedTime.gt(start), // 집계 시간 제외
                    usersProblemEntity.solvedTime.loe(endExclusive), // end = 배치 돌린 시각 포함
                    usersProblemEntity.solvedTime.goe(validAfterExpr),
                    teamFilter(team, userEntity)
            )
            .groupBy(userEntity.userId)
            .orderBy(
                    problemEntity.problemLevel.sum().desc(),
                    userEntity.username.asc()
            )
            .fetch();
  }

  private Predicate teamFilter(EurekaTeamName team, QUserEntity userEntity) {
    if (team == null) {
        return null;
    }
    return userEntity.teamName.eq(team);
  }
}
