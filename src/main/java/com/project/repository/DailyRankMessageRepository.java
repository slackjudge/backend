package com.project.repository;

import com.project.entity.DailyRankMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

/**
 * @author 김경민
 */
public interface DailyRankMessageRepository extends JpaRepository<DailyRankMessageEntity, Long> {

    /**==========================
     *
     * 최신 메시지부터 페이징 조회한다.
     *
     * @parm pageable 페이징 정보
     * @return List<DailyRankMessageEntity> 메시지 목록
     * @version 1.0.0
     * @date 2025-12-12
     *
    ==========================**/
    List<DailyRankMessageEntity> findAllByOrderByMessageIdDesc(Pageable pageable);


    /**==========================
    *
    * 특정 메시지 ID보다 작은 메시지를 최신순으로 조회한다.
    *
    * @parm lastId 기준 메시지 ID
    * @parm pageable 페이징 정보
    * @return List<DailyRankMessageEntity> 메시지 목록
    * @version 1.0.0
    * @date 2025-12-12
    *
    ==========================**/
    List<DailyRankMessageEntity> findByMessageIdLessThanOrderByMessageIdDesc(Long lastId, Pageable pageable);
}
