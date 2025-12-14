package com.project.common.util;

import com.project.dto.DailyRankInfo;
import java.util.List;
import org.springframework.stereotype.Component;

@Component
public class MessageFormatUtil {

  public String formatDailyRank(List<DailyRankInfo> ranks) {
    StringBuilder sb = new StringBuilder("🏆 오늘의 랭킹\n\n");

    for (DailyRankInfo r : ranks) {
      sb.append(medal(r.getRank()))
          .append(" ")
          .append(r.getRank())
          .append("위 ")
          .append(r.getName())
          .append(" - ")
          .append(r.getSolved())
          .append(" solved (+")
          .append(r.getScore())
          .append(")\n");
    }
    return sb.toString();
  }

  public String formatRankChange(String userName, int oldRank, int newRank, long score) {
    return String.format(
        """
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
