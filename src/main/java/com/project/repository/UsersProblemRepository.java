package com.project.repository;

import com.project.dto.DailyRankRawData;
import com.project.entity.UsersProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

public interface UsersProblemRepository extends JpaRepository<UsersProblemEntity, Long> {
    @Query("""
        SELECT new com.project.dto.DailyRankRawData(
            usr.userId,
            usr.username,
            COUNT(u),
            SUM(p.problemLevel)
        )
        FROM UsersProblemEntity u
         JOIN u.ref.user usr
         JOIN u.ref.problem p
        WHERE u.isSolved = true
          AND u.solvedTime BETWEEN :start AND :end
        GROUP BY usr.userId, usr.username
        ORDER BY SUM(p.problemLevel) DESC, usr.username ASC
    """)
    List<DailyRankRawData> findDailyRank(LocalDateTime start, LocalDateTime end);

}
