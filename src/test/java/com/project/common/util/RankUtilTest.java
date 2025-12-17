package com.project.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * author : 박준희
 */
class RankUtilTest {

    @Test
    @DisplayName("resolveBaseTime - null이면 예외")
    void resolveBaseTime_null_throws() {
        assertThatThrownBy(() -> RankUtil.resolveBaseTime(null))
                .isInstanceOf(IllegalArgumentException.class)
                .hasMessageContaining("baseTime must not be null");
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
        //given
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
    @DisplayName("getPeriodEndInclusive(day) - 같은 날(현재 진행) => now를 정각으로 내림")
    void getPeriodEndInclusive_day_current_returnsFlooredNow() {

        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 14, 39);
        LocalDateTime requested = LocalDateTime.of(2025, 12, 15, 9, 14);
        LocalDateTime endInclusive = RankUtil.getPeriodEndInclusive("day", requested, now);

        assertThat(endInclusive).isEqualTo(LocalDateTime.of(2025, 12, 15, 14, 0));
    }

    @Test
    @DisplayName("getPeriodEndExclusive(day) - 과거 날이면 requested 다음날 00:00")
    void getPeriodEndInclusive_day_pastDay_returnsNextDayStart() {
        //given
        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 9, 14);
        LocalDateTime requested = LocalDateTime.of(2025, 12, 11, 14, 39);

        //when
        LocalDateTime endExclusive = RankUtil.getPeriodEndInclusive("day", requested, now);

        //then
        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 12, 0, 0));
    }

    @Test
    @DisplayName("getPeriodEndInclusive(week) - 같은 주(현재 진행) => now를 정각으로 내림")
    void getPeriodEndInclusive_week_current_returnsFlooredNow() {

        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 14, 39);
        LocalDateTime requested = LocalDateTime.of(2025, 12, 16, 22, 10);
        LocalDateTime endExclusive = RankUtil.getPeriodEndInclusive("week", requested, now);

        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 15, 14, 0));
    }

    @Test
    @DisplayName("getPeriodEndInclusive(week) - 과거 주이면 해당 주 시작 + 1주 (다음주 월요일 00:00)")
    void getPeriodEndInclusive_week_pastWeek_returnsNextWeekStart() {
        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 9, 14);
        LocalDateTime requested = LocalDateTime.of(2025, 12, 8, 12, 0);

        LocalDateTime endExclusive = RankUtil.getPeriodEndInclusive("week", requested, now);

        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 15, 0, 0));
    }

    @Test
    @DisplayName("getPeriodEndInclusive(month) - 같은 달(현재 진행) => now를 정각으로 내림")
    void getPeriodEndInclusive_month_current_returnsFlooredNow() {
        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 14, 39);
        LocalDateTime requested = LocalDateTime.of(2025, 12, 5, 23, 0);
        LocalDateTime endExclusive = RankUtil.getPeriodEndInclusive("month", requested, now);

        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 15, 14, 0));
    }

    @Test
    @DisplayName("getPeriodEndInclusive(month) - 과거 달이면 해당 달 시작 + 1달 (다음달 1일 00:00)")
    void getPeriodEndInclusive_month_pastMonth_returnsNextMonthStart() {
        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 9, 14);
        LocalDateTime requested = LocalDateTime.of(2025, 11, 20, 9, 0);
        LocalDateTime endExclusive = RankUtil.getPeriodEndInclusive("month", requested, now);

        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 1, 0, 0));
    }
}
