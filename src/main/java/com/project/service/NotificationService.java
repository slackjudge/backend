package com.project.service;

import com.project.dto.response.DailyRankMessageResponse;
import com.project.entity.DailyRankMessageEntity;
import com.project.repository.DailyRankMessageRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.PageRequest;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private static final int MAX_PAGE_SIZE = 50;

    private final DailyRankMessageRepository dailyRankMessageRepository;

    public List<DailyRankMessageResponse> getNotifications(Long lastId, int size) {

        int pageSize = Math.min(size, MAX_PAGE_SIZE);
        List<DailyRankMessageEntity> entities;

        if (lastId == null) {
            entities = dailyRankMessageRepository
                    .findAllByOrderByMessageIdDesc(PageRequest.of(0, pageSize));
        } else {
            entities = dailyRankMessageRepository
                    .findByMessageIdLessThanOrderByMessageIdDesc(lastId, PageRequest.of(0, pageSize));
        }
        return entities.stream()
                .map(DailyRankMessageResponse::of)
                .toList();

    }
}
