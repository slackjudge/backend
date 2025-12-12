package com.project.repository;

import com.project.entity.DailyRankMessageEntity;
import org.springframework.data.jpa.repository.JpaRepository;

public interface DailyRankMessageRepository extends JpaRepository<DailyRankMessageEntity, Long> {
}
