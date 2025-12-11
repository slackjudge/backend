package com.project.common.util;

import com.project.dto.DailyRankInfo;
import org.springframework.stereotype.Component;

@Component
public class MessageFormatUtil {

    public String formatDailyRank(DailyRankInfo rank1, DailyRankInfo rank2, DailyRankInfo rank3) {
        return String.format("""
                        🏆 오늘 TOP 3

                        🥇 1위 %s — %d solved (+%d)
                        🥈 2위 %s — %d solved (+%d)
                        🥉 3위 %s — %d solved (+%d)""",
                rank1.getName(), rank1.getSolved(), rank1.getScore(),
                rank2.getName(), rank2.getSolved(), rank2.getScore(),
                rank3.getName(), rank3.getSolved(), rank3.getScore());
    }

    public String formatRankChange(String userName, int oldRank, int newRank, int score) {
        return String.format("""
                        🔥 %s님이 %d위 → %d위로 상승! 🔥

                        현재 점수: %d점""",
                                userName, oldRank, newRank, score);
    }
}
