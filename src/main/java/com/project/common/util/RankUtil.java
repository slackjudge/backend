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
     * period에 따른 "현재 구간 endExclusive" 계산
     *
     * 규칙:
     * - 오늘/이번주/이번달(=현재 진행 중인 기간) -> now 정각 + 1시간 (as-of)
     * - 과거 기간 -> 해당 기간의 끝(다음 day/week/month의 시작 00:00)
     *  현재 진행의 경우 -> 이번달 -> 요청한 날짜가 12.15일 경우 어차피 16일부터 데이터는 없기 때문에
     *  12.01 ~ 12.15일까지의 데이터만 수집하면됨
     *
     *  요청이 들어온게 과거일 경우(어제, 저번주, 저번달)
     *  request : 사용자가 저번주를 선택함 12.9일 -> 저번주
     *  now : 서버에 요청을 보낸 현재 시각  12.15일 -> 이번주
     *  과거의 경우 기간의 끝을 다음날 00:00, 다음주 월요일 00:00, 다음달 1일 00:00 으로 잡야아햠
     */
    public static LocalDateTime getPeriodEndExclusive(String period, LocalDateTime requested, LocalDateTime now) {
        LocalDateTime req = resolveBaseTime(requested);
        LocalDateTime cur = resolveBaseTime(now);

        return switch (period) {
            case "day" -> {
                boolean sameDay = req.toLocalDate().equals(cur.toLocalDate());
                yield sameDay
                        ? cur.plusHours(1)
                        : req.toLocalDate().plusDays(1).atStartOfDay(); // 다음날 00:00
            }

            case "week" -> {
                LocalDateTime reqWeekStart = getPeriodStart("week", req);
                LocalDateTime curWeekStart = getPeriodStart("week", cur);

                boolean sameWeek = reqWeekStart.equals(curWeekStart);
                yield sameWeek
                        ? cur.plusHours(1)
                        : reqWeekStart.plusWeeks(1); // 다음주 월요일 00:00
            }

            case "month" -> {
                LocalDateTime reqMonthStart = getPeriodStart("month", req);
                LocalDateTime curMonthStart = getPeriodStart("month", cur);

                boolean sameMonth = reqMonthStart.equals(curMonthStart);
                yield sameMonth
                        ? cur.plusHours(1)
                        : reqMonthStart.plusMonths(1); // 다음달 1일 00:00
            }

            default -> throw new IllegalArgumentException("invalid period: " + period);
        };
    }
}