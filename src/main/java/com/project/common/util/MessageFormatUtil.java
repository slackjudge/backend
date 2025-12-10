package com.project.common.util;

import org.springframework.stereotype.Component;

@Component
public class MessageFormatUtil {

    public String formatDailyRank(String rank1, int solved1, int score1,
                                     String rank2, int solved2, int score2,
                                     String rank3, int solved3, int score3) {
        return String.format("""
                        🏆 오늘 TOP 3

                        🥇 1위 %s — %d solved (+%d)
                        🥈 2위 %s — %d solved (+%d)
                        🥉 3위 %s — %d solved (+%d)""",
                rank1, solved1, score1, rank2, solved2, score2, rank3, solved3, score3);
    }

    public String formatRankChange(String userName, int oldRank, int newRank, int score) {
        return String.format("""
                        🔥 %s님이 %d위 → %d위로 상승! 🔥

                        현재 점수: %d점""",
                                userName, oldRank, newRank, score);
    }
}
