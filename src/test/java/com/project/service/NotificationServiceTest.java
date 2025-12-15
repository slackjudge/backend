package com.project.service;

import com.project.dto.response.DailyRankMessageResponse;
import com.project.entity.DailyRankMessageEntity;
import com.project.repository.DailyRankMessageRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;

import java.time.LocalDateTime;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.anyLong;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.mock;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @Mock
    DailyRankMessageRepository repository;

    @InjectMocks
    NotificationService notificationService;

    @Test
    @DisplayName("lastId가 없으면 최신 알림부터 조회한다")
    void getNotifications_firstPage() {
        // given
        DailyRankMessageEntity entity = mockEntity(10L);

        when(repository.findAllByOrderByMessageIdDesc(any(PageRequest.class)))
                .thenReturn(List.of(entity));

        // when
        List<DailyRankMessageResponse> result =
                notificationService.getNotifications(null, 20);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(10L);

        verify(repository, times(1))
                .findAllByOrderByMessageIdDesc(any(PageRequest.class));
        verify(repository, never())
                .findByMessageIdLessThanOrderByMessageIdDesc(anyLong(), any());
    }

    @Test
    @DisplayName("lastId가 있으면 cursor 기반으로 조회한다")
    void getNotifications_withCursor() {
        // given
        DailyRankMessageEntity entity = mockEntity(5L);

        when(repository.findByMessageIdLessThanOrderByMessageIdDesc(
                eq(6L),
                any(PageRequest.class)
        )).thenReturn(List.of(entity));

        // when
        List<DailyRankMessageResponse> result =
                notificationService.getNotifications(6L, 20);

        // then
        assertThat(result).hasSize(1);
        assertThat(result.get(0).getId()).isEqualTo(5L);

        verify(repository, times(1))
                .findByMessageIdLessThanOrderByMessageIdDesc(eq(6L), any());
        verify(repository, never())
                .findAllByOrderByMessageIdDesc(any());
    }

    private DailyRankMessageEntity mockEntity(Long id) {
        DailyRankMessageEntity entity = mock(DailyRankMessageEntity.class);
        when(entity.getMessageId()).thenReturn(id);
        when(entity.getMessageContent()).thenReturn("test message");
        when(entity.getCreatedAt()).thenReturn(LocalDateTime.of(2025, 12, 1, 0, 0));
        return entity;
    }
}
