package com.project.repository;

import com.project.dto.response.GrassResponse;
import com.project.dto.response.ProblemResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.core.types.dsl.Expressions;
import com.querydsl.core.types.dsl.StringExpression;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

import static com.project.entity.QProblemEntity.problemEntity;
import static com.project.entity.QUsersProblemEntity.usersProblemEntity;

/*
author : 최하영
*/
@Repository
@RequiredArgsConstructor
public class MyPageRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 월간 잔디 데이터 조회
     * @param userId
     * @param year
     * @param month
     * @param validAfter
     * @return
     */
    public List<GrassResponse> findGrassList(Long userId, int year, int month, LocalDateTime validAfter) {
        LocalDateTime startOfMonth = LocalDate.of(year, month, 1).atStartOfDay();
        LocalDateTime endOfMonth = LocalDate.of(year, month, 1).plusMonths(1).minusDays(1).atTime(23, 59, 59);

        StringExpression dateStr =
                Expressions.stringTemplate("TO_CHAR({0}, 'YYYY-MM-DD')", usersProblemEntity.solvedTime)
                        .stringValue();

        return queryFactory
                .select(Projections.constructor(
                        GrassResponse.class,
                        dateStr,
                        usersProblemEntity.count().intValue()
                ))
                .from(usersProblemEntity)
                .where(
                        usersProblemEntity.ref.user.userId.eq(userId),
                        usersProblemEntity.solvedTime.between(startOfMonth, endOfMonth),
                        usersProblemEntity.solvedTime.goe(validAfter)
                )
                .groupBy(dateStr)
                .orderBy(dateStr.asc())
                .fetch();
    }

    /**
     * 특정 날짜에 푼 문제 상세 목록 조회
     * @param userId
     * @param date
     * @param validAfter
     * @return
     */
    public List<ProblemResponse> findSolvedProblemList(Long userId, LocalDate date, LocalDateTime validAfter) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59, 999_999_999);

        return queryFactory
                .select(Projections.constructor(ProblemResponse.class,
                        problemEntity.problemTitle,
                        problemEntity.problemLevel,
                        problemEntity.problemUrl))
                .from(usersProblemEntity)
                .join(usersProblemEntity.ref.problem, problemEntity)
                .where(
                        usersProblemEntity.ref.user.userId.eq(userId),
                        usersProblemEntity.isSolved.isTrue(),
                        usersProblemEntity.solvedTime.between(startOfDay, endOfDay),
                        usersProblemEntity.solvedTime.goe(validAfter)
                )
                .orderBy(usersProblemEntity.solvedTime.asc())
                .fetch();
    }
}
