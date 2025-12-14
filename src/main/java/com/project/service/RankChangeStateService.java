package com.project.service;

import com.project.common.util.MessageFormatUtil;
import com.project.common.util.SlackMessageSender;
import com.project.dto.RankRawData;
import com.project.entity.RankChangeStateEntity;
import com.project.entity.UserEntity;
import com.project.repository.RankChangeStateRepository;
import com.project.repository.UserRepository;
import com.project.repository.UsersProblemRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankChangeStateService {

    private final UsersProblemRepository usersProblemRepository;
    private final RankChangeStateRepository rankChangeStateRepository;
    private final SlackMessageSender slackMessageSender;
    private final MessageFormatUtil messageFormatUtil;
    private final UserRepository userRepository;

    @Transactional
    public void sendRankChangeMessage() {
        LocalDateTime monthStart = LocalDate.now()
                .withDayOfMonth(1)
                .atStartOfDay();

        List<RankRawData> raw = usersProblemRepository.findMonthlyRank(monthStart);
        if (raw.isEmpty()) return;

        Map<Long, Integer> currentRanks = calculateRank(raw);

        Map<Long, Long> scoreMap = raw.stream()
                .collect(Collectors.toMap(RankRawData::getUserId, RankRawData::getScore));

        List<Long> userIds = new ArrayList<>(currentRanks.keySet());

        Map<Long, RankChangeStateEntity> stateMap = rankChangeStateRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(RankChangeStateEntity::getUserId, s -> s));

        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, u -> u));

        List<RankChangeStateEntity> statesToSave = new ArrayList<>();

        for (Long userId : userIds) {
            int currentRank = currentRanks.get(userId);
            RankChangeStateEntity state = stateMap.get(userId);

            // 최초 유저
            if (state == null) {
                statesToSave.add(RankChangeStateEntity.create(userId, currentRank));
                continue;
            }

            int lastRank = state.getLastCheckedRank();

            UserEntity user = userMap.get(userId);
            boolean canNotify = user != null && user.isAlertAgreed();

            if (canNotify && currentRank < lastRank) {
                try {
                    String message = messageFormatUtil.formatRankChange(
                            user.getUsername(),
                            lastRank,
                            currentRank,
                            scoreMap.getOrDefault(userId, 0L)
                    );
                    slackMessageSender.sendMessage(user.getSlackId(), message);
                } catch (Exception e) {
                    log.warn("순위 변동 DM 실패 userId={}, err={}", userId, e.getMessage());
                }
            }
            state.updateRank(currentRank);
            statesToSave.add(state);
        }
        if (!statesToSave.isEmpty()) {
            rankChangeStateRepository.saveAll(statesToSave);
        }
    }

    private Map<Long, Integer> calculateRank(List<RankRawData> raw) {
        Map<Long, Integer> rankMap = new HashMap<>();
        int rank = 1;

        for (int i = 0; i < raw.size(); i++) {
            if (i > 0 && !raw.get(i - 1).getScore().equals(raw.get(i).getScore())) {
                rank = i + 1;
            }
            rankMap.put(raw.get(i).getUserId(), rank);
        }
        return rankMap;
    }
}
