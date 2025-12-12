package com.project.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.assertj.core.api.Assertions.assertThat;

class RankUtilTest {

    @Test
    @DisplayName("resolveBaseTime - null값 테스트")
    void resolvedTime_null(){
        //given
        LocalDate today = LocalDate.now();

        //when
        LocalDateTime base = RankUtil.resolveBaseTime(null);

        //then
        assertThat(base.getMonth()).isEqualTo(today.getMonth());
        assertThat(base.getDayOfMonth()).isEqualTo(today.getDayOfMonth());
        assertThat(base.getMinute()).isZero();
        assertThat(base.getSecond()).isZero();
        assertThat(base.getNano()).isZero();
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
    @DisplayName("getPeriodStart - day일 때는 해당 날짜 00시")
    void getPeriodStart_day() {
        //given
        LocalDateTime base = LocalDateTime.of(2025, 12, 11, 14, 30);

        //when
        LocalDateTime start = RankUtil.getPeriodStart("day", base);

        //then
        assertThat(start).isEqualTo(LocalDateTime.of(2025, 12, 11, 0, 0));
    }

    @Test
    @DisplayName("getPeriodStart - week일 때는 해당 주 월요일 00시")
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
    @DisplayName("getPeriodStart - month일 때는 해당 달 1일 00시")
    void getPeriodStart_month() {
        //given
        LocalDateTime base = LocalDateTime.of(2025, 12, 11, 14, 30);

        //when
        LocalDateTime start = RankUtil.getPeriodStart("month", base);

        //then
        assertThat(start).isEqualTo(LocalDateTime.of(2025, 12, 1, 0, 0));
    }

    @Test
    @DisplayName("getCurrentEnd - 기준 시각의 정각(시)까지를 현재 구간 끝으로 사용")
    void getCurrentEnd() {
        //given
        LocalDateTime input = LocalDateTime.of(2025, 12, 11, 14, 30, 10);

        //when
        LocalDateTime currentEnd = RankUtil.getCurrentEnd(input);

        //then
        assertThat(currentEnd).isEqualTo(LocalDateTime.of(2025, 12, 11, 14, 0));
    }

    @Test
    @DisplayName("getPrevEnd - 이전 구간 끝은 현재 구간 끝에서 1시간 전")
    void getPrevEnd() {
        //given
        LocalDateTime input = LocalDateTime.of(2025, 12, 11, 14, 30);

        //when
        LocalDateTime prevEnd = RankUtil.getPrevEnd(input);

        //then
        assertThat(prevEnd).isEqualTo(LocalDateTime.of(2025, 12, 11, 13, 0));
    }



}
