package com.project.repository;

import com.project.entity.DailyRankMessageEntity;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;

public interface DailyRankMessageRepository extends JpaRepository<DailyRankMessageEntity, Long> {

    List<DailyRankMessageEntity> findAllByOrderByMessageIdDesc(Pageable pageable);

    List<DailyRankMessageEntity> findByMessageIdLessThanOrderByMessageIdDesc(Long lastId, Pageable pageable);
}
