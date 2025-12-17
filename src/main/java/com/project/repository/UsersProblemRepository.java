package com.project.repository;

import com.project.dto.RankRawData;
import com.project.entity.UsersProblemEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.time.LocalDateTime;
import java.util.List;

/**
 * @author 김경민
 */
public interface UsersProblemRepository extends JpaRepository<UsersProblemEntity, Long> {
    @Query("""
        SELECT new com.project.dto.RankRawData(
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
    List<RankRawData> findDailyRank(LocalDateTime start, LocalDateTime end);

}
