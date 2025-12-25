package com.project.repository;

import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.Optional;

@Repository
@RequiredArgsConstructor
public class BatchMetaRepository {

    private final JdbcTemplate jdbcTemplate;

    /*==========================
    * @author : 박준희
    * 배치 스냅샷 테이블에서 가장 최신 스냅샷 가져오기
    * 현재 job이 하나만 있다고 생각하고 작성한 쿼리임
    ==========================**/
    public Optional<LocalDateTime> findLastCompletedEndTime() {
        String sql = """
            select bje.end_time
            from batch_job_execution bje
            where bje.status = 'COMPLETED'
              and bje.end_time is not null
            order by bje.end_time desc, bje.job_execution_id desc
            limit 1
            """;

        return jdbcTemplate.query(sql, rs -> {
            if (!rs.next()) {
                return Optional.empty();
            }
          LocalDateTime endTime =  rs.getObject(1, LocalDateTime.class);
            return Optional.ofNullable(endTime);
        });
    }
}
