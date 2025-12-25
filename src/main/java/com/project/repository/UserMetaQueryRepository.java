package com.project.repository;

import com.project.entity.QUserEntity;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * author : 박준희
 * 신규 유저를 확인하는 쿼리 -> 가입시간, id를 반환
 */
@Repository
@RequiredArgsConstructor
public class UserMetaQueryRepository {

    private final JPAQueryFactory queryFactory;

    public Map<Long, LocalDateTime> findCreatedAtMapByUserIds(List<Long> userIds) {
        if (userIds == null || userIds.isEmpty()) {
            return Map.of();
        }

        QUserEntity u = QUserEntity.userEntity;

        return queryFactory
                .select(u.userId, u.createdAt)
                .from(u)
                .where(u.userId.in(userIds))
                .fetch()
                .stream()
                .collect(Collectors.toMap(
                        t -> t.get(u.userId),
                        t -> t.get(u.createdAt)
                ));
    }
}