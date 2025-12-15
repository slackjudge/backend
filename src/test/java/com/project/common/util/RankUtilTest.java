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
    @DisplayName("getPeriodEndExclusive - 현재의 경우 - day 절삭 후 +1시간 (endExclusive)")
    void getPeriodEndExclusive_returnsNextHourExclusive() {
        //given 선택한 시각 : 12.15일 09:14분 // 현재 서버의 시각 : 2025.12.15 14:39분 같은날 요청
        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 9, 14);        // cur=09:00
        LocalDateTime requested = LocalDateTime.of(2025, 12, 15, 14, 39); // req=14:00 (sameDay)

        //when
        LocalDateTime endExclusive = RankUtil.getPeriodEndExclusive("day", requested, now);

        //then
        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 15, 10, 0)); // cur+1h0));
    }

    @Test
    @DisplayName("getPeriodEndExclusive(day) - 과거 날이면 requested 다음날 00:00")
    void getPeriodEndExclusive_day_pastDay_returnsNextDayStart() {
        //given 요청 시각 : 12.11일 14시 // 현재 서버 시각 : 12.15잉ㄹ 9시:40분
        // 과거에 대한 요청이기 때문에 [12.15일 00:00, 12.16일 00:00) 조회
        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 9, 14);
        LocalDateTime requested = LocalDateTime.of(2025, 12, 11, 14, 39);

        //when
        LocalDateTime endExclusive = RankUtil.getPeriodEndExclusive("day", requested, now);

        //then
        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 12, 0, 0));
    }

    @Test
    @DisplayName("getPeriodEndExclusive(week) - 같은 주이면 now 정각 + 1시간")
    void getPeriodEndExclusive_week_sameWeek_returnsNowPlus1Hour() {
        // now=2025-12-15(월) 09:14 => weekStart=12/15 00:00
        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 9, 14);
        // requested도 같은 주(12/15~)에 속하도록 12/16로 설정
        LocalDateTime requested = LocalDateTime.of(2025, 12, 16, 22, 10);

        LocalDateTime endExclusive = RankUtil.getPeriodEndExclusive("week", requested, now);

        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 15, 10, 0)); // cur+1h
    }

    @Test
    @DisplayName("getPeriodEndExclusive(week) - 과거 주이면 해당 주 시작 + 1주 (다음주 월요일 00:00)")
    void getPeriodEndExclusive_week_pastWeek_returnsNextWeekStart() {
        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 9, 14);
        LocalDateTime requested = LocalDateTime.of(2025, 12, 8, 12, 0); // 12/08 주

        LocalDateTime endExclusive = RankUtil.getPeriodEndExclusive("week", requested, now);

        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 15, 0, 0)); // 12/08 + 1주 => 12/15 00:00
    }

    @Test
    @DisplayName("getPeriodEndExclusive(month) - 같은 달이면 now 정각 + 1시간")
    void getPeriodEndExclusive_month_sameMonth_returnsNowPlus1Hour() {
        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 9, 14);
        LocalDateTime requested = LocalDateTime.of(2025, 12, 5, 23, 0); // 12월

        LocalDateTime endExclusive = RankUtil.getPeriodEndExclusive("month", requested, now);

        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 15, 10, 0)); // cur+1h
    }

    @Test
    @DisplayName("getPeriodEndExclusive(month) - 과거 달이면 해당 달 시작 + 1달 (다음달 1일 00:00)")
    void getPeriodEndExclusive_month_pastMonth_returnsNextMonthStart() {
        LocalDateTime now = LocalDateTime.of(2025, 12, 15, 9, 14);
        LocalDateTime requested = LocalDateTime.of(2025, 11, 20, 9, 0); // 11월

        LocalDateTime endExclusive = RankUtil.getPeriodEndExclusive("month", requested, now);

        assertThat(endExclusive).isEqualTo(LocalDateTime.of(2025, 12, 1, 0, 0)); // 11/01 +1달 => 12/01 00:00
    }


}
