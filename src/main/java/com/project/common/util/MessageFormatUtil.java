package com.project.common.util;

import com.project.dto.DailyRankInfo;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
public class MessageFormatUtil {

    public String formatDailyRank(List<DailyRankInfo> ranks) {
        StringBuilder sb = new StringBuilder("🏆 오늘의 랭킹\n\n");

        for (DailyRankInfo r : ranks) {
            sb.append(String.format(
               "%s %d위 %s - %d solved (+%d)\n",
                medal(r.getRank()), r.getRank(), r.getName(), r.getSolved(), r.getScore()
            ));
        }
        return sb.toString();
    }

    public String formatRankChange(String userName, int oldRank, int newRank, int score) {
        return String.format("""
                        🔥 %s님이 %d위 → %d위로 상승! 🔥

                        현재 점수: %d점""",
                                userName, oldRank, newRank, score);
    }

    private String medal(int rank) {
        return switch (rank) {
            case 1 -> "🥇";
            case 2 -> "🥈";
            case 3 -> "🥉";
            default -> "";
        };
    }
}
