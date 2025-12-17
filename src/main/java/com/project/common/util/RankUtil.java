package com.project.common.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;


/**
 * author : 박준희
 */
public class RankUtil {


    /**
     * 정각으로 절삭 하는 유틸 함수, 배치 시간 텀에 따라 유동적 변경
     * @param baseTime
     * @return baseTime의 정각 시간
     */
    public static LocalDateTime resolveBaseTime(LocalDateTime baseTime) {
        if (baseTime == null) {
            throw new IllegalArgumentException("baseTime must not be null");
        }
        return baseTime.withMinute(0).withSecond(0).withNano(0);
    }


    /**
     * period에 따른 집계 시작 시각 계산
     *  return 값
     *  - day   : 해당 날짜의 00:00
     *  - week  : 해당 주 월요일 00:00
     *  - month : 해당 달 1일 00:00
     */
    public static LocalDateTime getPeriodStart(String period, LocalDateTime baseTime) {

        LocalDateTime t = resolveBaseTime(baseTime);

        return switch (period) {
            case "week" -> t
                    .with(DayOfWeek.MONDAY)
                    .toLocalDate()
                    .atStartOfDay();
            case "month" -> t
                    .withDayOfMonth(1)
                    .toLocalDate()
                    .atStartOfDay();
            default -> t.toLocalDate().atStartOfDay();
        };
    }

    /**
     * 요청한 기간에 따른 과거, 현재 비교해 해당 기간에 맞는 집계 마감 시각 계산
     * @param period
     * @param requested
     * @param now
     * @return 집계 마감 시각(정각)
     */
    public static LocalDateTime getPeriodEndInclusive(String period, LocalDateTime requested, LocalDateTime now) {
        LocalDateTime req = resolveBaseTime(requested);
        LocalDateTime cur = resolveBaseTime(now);

        LocalDateTime reqStart = getPeriodStart(period, req);
        LocalDateTime curStart = getPeriodStart(period, cur);

        boolean isCurrentPeriod = reqStart.equals(curStart);

        if (isCurrentPeriod) {
            return cur;
        }

        return switch (period) {
            case "week" -> reqStart.plusWeeks(1);
            case "month" -> reqStart.plusMonths(1);
            case "day" -> reqStart.plusDays(1);
            default -> throw new IllegalArgumentException("invalid period: " + period);
        };
    }
}