package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.MessageFormatUtil;
import com.project.common.util.SlackChannelResolver;
import com.project.common.util.SlackMessageSender;
import com.project.dto.DailyRankInfo;
import com.project.dto.RankRawData;
import com.project.entity.DailyRankMessageEntity;
import com.project.entity.UserEntity;
import com.project.repository.DailyRankMessageRepository;
import com.project.repository.UserRepository;
import com.project.repository.UsersProblemRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.temporal.ChronoUnit;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class DailyRankMessageService {

    private final UserRepository userRepository;
    private final UsersProblemRepository usersProblemRepository;
    private final SlackMessageSender slackMessageSender;
    private final MessageFormatUtil messageFormatUtil;
    private final SlackChannelResolver slackChannelResolver;
    private final DailyRankMessageRepository dailyRankMessageRepository;

    private static final int RANKING_LIMIT = 3;

    public void sendDailyRankMessage() {
        LocalDateTime start = LocalDate.now().atStartOfDay();
        LocalDateTime end = LocalDateTime.now();

        List<RankRawData> raw = usersProblemRepository.findDailyRank(start, end);

        Map<Long, UserEntity> userMap = userRepository
                .findAllById(raw.stream().map(RankRawData::getUserId).toList())
                .stream()
                .collect(Collectors.toMap(UserEntity::getUserId, u -> u));

        List<RankRawData> filtered = raw.stream()
                .filter(r -> {
                    UserEntity user = userMap.get(r.getUserId());
                    LocalDateTime validAfter =
                            user.getCreatedAt().truncatedTo(ChronoUnit.HOURS)
                                    .plusHours(2);
                    return validAfter.isBefore(end);
                })
                .toList();

        List<DailyRankInfo> ranked = calculateRank(filtered);

        String message;
        if (ranked.isEmpty()) {
            message = "오늘은 새로운 문제 풀이가 없습니다.😢";
        } else {
            message = messageFormatUtil.formatDailyRank(ranked);
        }

        try {
            slackMessageSender.sendMessage(slackChannelResolver.dailyRank(), message);
            dailyRankMessageRepository.save(DailyRankMessageEntity.of(message));
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(
                    ErrorCode.SLACK_MESSAGE_FAILED, "slack 메시지 전송 중 오류 발생 : " + e.getMessage());
        }
    }


    private List<DailyRankInfo> calculateRank(List<RankRawData> raw) {
        if (raw.isEmpty()) return List.of();

        List<DailyRankInfo> ranked = new ArrayList<>();
        int currentRank = 1;

            for (int i = 0; i < raw.size(); i++) {
                RankRawData r = raw.get(i);

                if (i > 0 && !raw.get(i - 1).getScore().equals(r.getScore())) {
                    currentRank = i + 1;
                }

                if (currentRank > RANKING_LIMIT) break;

                ranked.add(new DailyRankInfo(r.getUsername(), r.getSolvedCount(), r.getScore(), currentRank));
            }
        return ranked;
      }
}
