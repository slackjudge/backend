package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.MessageFormatUtil;
import com.project.common.util.SlackChannelResolver;
import com.project.common.util.SlackMessageSender;
import com.project.dto.DailyRankInfo;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.DailyRankMessageEntity;
import com.project.repository.DailyRankMessageRepository;
import com.project.repository.RankingQueryRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class DailyRankMessageService {

    private final SlackMessageSender slackMessageSender;
    private final MessageFormatUtil messageFormatUtil;
    private final SlackChannelResolver slackChannelResolver;
    private final DailyRankMessageRepository dailyRankMessageRepository;
    private final RankingQueryRepository rankingQueryRepository;

    private static final int RANKING_LIMIT = 3;

    public void sendDailyRankMessage() {
        LocalDateTime startOfDay = LocalDate.now().atStartOfDay();
        LocalDateTime now = LocalDateTime.now();

        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(
                        startOfDay,
                        now,
                        null // 팀 필터 없음
                );

        List<DailyRankInfo> ranked = calculateTopRank(rows);

        String message = ranked.isEmpty()
                ? "오늘은 새로운 문제 풀이가 없습니다.😢"
                : messageFormatUtil.formatDailyRank(ranked);

        try {
            String channelId = slackChannelResolver.dailyRank();
            log.info("[DailyRank] send channelId={}", channelId);

            slackMessageSender.sendMessage(channelId, message);
            dailyRankMessageRepository.save(DailyRankMessageEntity.of(message));

        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SLACK_MESSAGE_FAILED,
                    "slack 메시지 전송 중 오류 발생 : " + e.getMessage()
            );
        }
    }


    private List<DailyRankInfo> calculateTopRank(List<RankingRowResponse> rows) {
        List<DailyRankInfo> result = new ArrayList<>();

        int currentRank = 1;

        for (int i = 0; i < rows.size(); i++) {
            if (i >= RANKING_LIMIT) break;

            RankingRowResponse r = rows.get(i);

            if (i > 0 && rows.get(i - 1).getTotalScore() != r.getTotalScore()) {
                currentRank = i + 1;
            }

            result.add(new DailyRankInfo(
                    r.getName(),
                    r.getSolvedCount(),
                    r.getTotalScore(),
                    currentRank
            ));
        }
        return result;
    }
}
