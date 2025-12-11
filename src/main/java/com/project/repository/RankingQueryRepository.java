package com.project.repository;

import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.jpa.repository.query.JpaQueryMethodFactory;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class RankingQueryRepository {

  private final JPAQueryFactory queryFactory;
}
