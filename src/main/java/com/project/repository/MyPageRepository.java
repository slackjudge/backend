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

@Repository
@RequiredArgsConstructor
public class MyPageRepository {

    private final JPAQueryFactory queryFactory;

    /**
     * 월간 잔디 데이터 조회
     */
    public List<GrassResponse> findGrassList(Long userId, int year, int month) {
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
                        usersProblemEntity.user.userId.eq(userId),
                        usersProblemEntity.solvedTime.between(startOfMonth, endOfMonth))
                .groupBy(dateStr)
                .fetch();
    }

    /**
     * 특정 날짜에 푼 문제 상세 목록 조회
     */
    public List<ProblemResponse> findSolvedProblemList(Long userId, LocalDate date) {
        LocalDateTime startOfDay = date.atStartOfDay();
        LocalDateTime endOfDay = date.atTime(23, 59, 59);

        return queryFactory
                .select(Projections.constructor(ProblemResponse.class,
                        problemEntity.problemTitle,
                        problemEntity.problemLevel,
                        problemEntity.problemUrl))
                .from(usersProblemEntity)
                .join(usersProblemEntity.problem, problemEntity)
                .where(
                        usersProblemEntity.user.userId.eq(userId),
                        usersProblemEntity.isSolved.isTrue(),
                        usersProblemEntity.solvedTime.between(startOfDay,endOfDay))
                .orderBy(usersProblemEntity.solvedTime.asc())
                .fetch();
    }

}
