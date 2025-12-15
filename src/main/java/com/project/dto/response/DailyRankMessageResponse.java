package com.project.dto.response;

import com.project.entity.DailyRankMessageEntity;
import lombok.AllArgsConstructor;
import lombok.Getter;

import java.time.format.DateTimeFormatter;

@Getter
@AllArgsConstructor
public class DailyRankMessageResponse {

    private static final DateTimeFormatter DATE_TIME_FORMATTER = DateTimeFormatter.ofPattern("M월 d일");

    private Long id;

    private String message;

    private String date;

    public static DailyRankMessageResponse of(DailyRankMessageEntity entity) {
        return new DailyRankMessageResponse(
                entity.getMessageId(),
                entity.getMessageContent(),
                entity.getCreatedAt().format(DATE_TIME_FORMATTER)
        );
    }
}
