package com.project.common.util;

import java.time.DayOfWeek;
import java.time.LocalDateTime;

public class RankUtil {


    /**
     * 기준 시각 정규화
     * - null이면 now
     * - 분/초/나노초를 0으로 맞춰서 정각으로 남김(ex: 14:30 -> 14:00)
     * 2025-12-11T14:37:00 => 2025-12-11T14:00:00
     */
    public static LocalDateTime resolveBaseTime(LocalDateTime dateTime) {
        LocalDateTime base = (dateTime != null) ? dateTime : LocalDateTime.now();
        return base.withMinute(0).withSecond(0).withNano(0);
    }


    /**
     * period에 따른 집계 시작 일자 계산
     *  return 값
     *  - day   : 해당 날짜의 00:00
     *  - week  : 해당 주 월요일 00:00
     *  - month : 해당 달 1일 00:00
     */
    public static LocalDateTime getPeriodStart(String period, LocalDateTime baseTime) {
        // 정각으로 설정
        LocalDateTime t = resolveBaseTime(baseTime);

        return switch (period) {
            case "week" -> t
                    .with(DayOfWeek.MONDAY)
                    .toLocalDate()
                    .atStartOfDay(); // 해당 주의 월요일 00:00
            case "month" -> t
                    .withDayOfMonth(1)
                    .toLocalDate()
                    .atStartOfDay(); // 해당 달 1일의 00:00
            default -> t.toLocalDate().atStartOfDay(); // 해당 날짜 00:00
        };
    }


    /**
     * 랭킹 조회용 기간 끝 시각 (exclusive)
     * baseTime = 2025-12-11T14:37 ->
     *   resolveBaseTime(baseTime) = 2025-12-11T14:00
     *   리턴 = 2025-12-11T15:00
     * 즉 [start, endExclusive) 에서 endExclusive로 사용
     */
    public static LocalDateTime getPeriodEndExclusive(LocalDateTime baseTime) {
        return resolveBaseTime(baseTime).plusHours(1);
    }



    /**
     * 현재 구간 끝 시각 (inclusive)
     *  - baseTime이 2025-12-08T14:30이면 → 2025-12-08T14:00
     */
    public static LocalDateTime getCurrentEnd(LocalDateTime baseTime) {
        return resolveBaseTime(baseTime);
    }


    /**
     * 이전 구간 끝 시각 (inclusive)
     *  - baseTime이 2025-12-08T14:30이면 → 2025-12-08T13:00
     */
    public static LocalDateTime getPrevEnd(LocalDateTime baseTime) {
        return getCurrentEnd(baseTime).minusHours(1);
    }

}
