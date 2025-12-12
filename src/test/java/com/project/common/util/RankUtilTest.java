package com.project.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RankUtilTest {

    @Test
    @DisplayName("resolveBaseTime - null이면 예외")
    void resolveBaseTime_null_throws() {
        assertThatThrownBy(() -> RankUtil.resolveBaseTime(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("초기 시간 값은 Null이 될 수 없습니다.");
    }


    @Test
    @DisplayName("resolveBaseTime - 정규화 테스트")
    void resolveBaseTime_nonNull() {
        //given
        LocalDateTime input = LocalDateTime.of(2025, 12, 11, 14, 37, 45, 123_000_000);

        //when
        LocalDateTime base = RankUtil.resolveBaseTime(input);

        //then
        assertThat(base).isEqualTo(LocalDateTime.of(2025, 12, 11, 14, 0, 0, 0));
    }

    @Test
    @DisplayName("getPeriodStart - day일 때는 해당 날짜 00:00")
    void getPeriodStart_day() {
        //given
        LocalDateTime base = LocalDateTime.of(2025, 12, 11, 14, 30);

        //when
        LocalDateTime start = RankUtil.getPeriodStart("day", base);

        //then
        assertThat(start).isEqualTo(LocalDateTime.of(2025, 12, 11, 0, 0));
    }

    @Test
    @DisplayName("getPeriodStart - week일 때는 해당 주 월요일 00:00")
    void getPeriodStart_week() {
        //given 2025-12-11 : 목요일이라고 가정
        LocalDateTime base = LocalDateTime.of(2025, 12, 11, 14, 30);

        //when
        LocalDateTime start = RankUtil.getPeriodStart("week", base);

        //then
        assertThat(start.getDayOfWeek()).isEqualTo(DayOfWeek.MONDAY);
        assertThat(start.getHour()).isZero();
        assertThat(start.getMinute()).isZero();
    }

    @Test
    @DisplayName("getPeriodStart - month일 때는 해당 달 1일 00:00")
    void getPeriodStart_month() {
        //given
        LocalDateTime base = LocalDateTime.of(2025, 12, 11, 14, 30);

        //when
        LocalDateTime start = RankUtil.getPeriodStart("month", base);

        //then
        assertThat(start).isEqualTo(LocalDateTime.of(2025, 12, 1, 0, 0));
    }

    @Test
    @DisplayName("getPeriodEndExclusive - 기준 시각을 정각으로 절삭 후 +1시간 (endExclusive)")
    void getPeriodEndExclusive_returnsNextHourExclusive() {
        // given
        LocalDateTime input = LocalDateTime.of(2025, 12, 11, 14, 30, 10);

        // when
        LocalDateTime endExclusive = RankUtil.getPeriodEndExclusive(input);

        // then: 14:30 -> (정각 절삭 14:00) + 1h = 15:00
        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 11, 15, 0));
    }
}
