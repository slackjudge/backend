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
    public static LocalDateTime resolveBaseTime(LocalDateTime baseTime) {
        if (baseTime == null) {
            throw new IllegalArgumentException("baseTime must not be null");
        }
        return baseTime.withMinute(0).withSecond(0).withNano(0);
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
     * period에 따른 "현재 구간 endInclusive" 계산
     *
     * 규칙:
     * 1) 요청한 period가 "현재 진행 중인 기간"이면:
     *    - DB에 존재 가능한 최신 스냅샷은 floor(now) 이므로 endInclusive = floor(now)
     *
     * 2) 요청한 period가 "과거 기간"이면:
     *    - 그 기간의 마지막 스냅샷을 포함하려면 endInclusive는 "기간 끝(다음 기간의 시작 00:00)" 이어야 함
     *      day   -> 다음날 00:00
     *      week  -> 다음주 월요일 00:00
     *      month -> 다음달 1일 00:00
     *
     * 3) 요청이 미래면(방어): -> 프론트에서 클릭 안되게 해놨음
     *    - 데이터가 없으므로 endInclusive를 floor(now)로 캡(clamp)
     */
    public static LocalDateTime getPeriodEndInclusive(String period, LocalDateTime requested, LocalDateTime now) {
        LocalDateTime req = resolveBaseTime(requested);
        LocalDateTime cur = resolveBaseTime(now);

        LocalDateTime reqStart = getPeriodStart(period, req);
        LocalDateTime curStart = getPeriodStart(period, cur);

        boolean isCurrentPeriod = reqStart.equals(curStart);

        // 현재 진행 -> 최신 스냅샷은 floor(now)
        if (isCurrentPeriod) {
            return cur;
        }

        // 과거 기간: 기간 끝(다음 기간 시작 00:00) 스냅샷까지 포함
        return switch (period) {
            case "week" -> reqStart.plusWeeks(1);
            case "month" -> reqStart.plusMonths(1);
            case "day" -> reqStart.plusDays(1);
            default -> throw new IllegalArgumentException("invalid period: " + period);
        };
    }
}