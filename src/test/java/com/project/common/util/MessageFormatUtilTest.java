package com.project.common.util;

import com.project.dto.DailyRankInfo;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MessageFormatUtilTest {

    MessageFormatUtil util = new MessageFormatUtil();

    @Test
    @DisplayName("일일 랭킹 메시지 포맷팅 검증")
    void dailyRankFormatTest() {
        DailyRankInfo r1 = new DailyRankInfo("유재석", 7, 48);
        DailyRankInfo r2 = new DailyRankInfo("정형돈", 5, 32);
        DailyRankInfo r3 = new DailyRankInfo("노홍철", 4, 30);

        String expected = """
            🏆 오늘 TOP 3

            🥇 1위 유재석 — 7 solved (+48)
            🥈 2위 정형돈 — 5 solved (+32)
            🥉 3위 노홍철 — 4 solved (+30)""";

        String result = util.formatDailyRank(r1, r2, r3);

        assertThat(result).isEqualTo(expected);
    }

    @Test
    @DisplayName("랭킹 변동 메시지 포맷팅 검증")
    void rankChangeFormatTest() {
        String result = util.formatRankChange("박명수", 5, 3, 26);

        String expected = """
                🔥 박명수님이 5위 → 3위로 상승! 🔥
                
                현재 점수: 26점""";

        assertThat(result).isEqualTo(expected);
    }

}
