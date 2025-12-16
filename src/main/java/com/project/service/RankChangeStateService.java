package com.project.service;

import com.project.common.util.MessageFormatUtil;
import com.project.common.util.SlackMessageSender;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.RankChangeStateEntity;
import com.project.entity.UserEntity;
import com.project.repository.RankChangeStateRepository;
import com.project.repository.RankingQueryRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class RankChangeStateService {

    private final RankChangeStateRepository rankChangeStateRepository;
    private final SlackMessageSender slackMessageSender;
    private final MessageFormatUtil messageFormatUtil;
    private final UserRepository userRepository;
    private final RankingQueryRepository rankingQueryRepository;

    @Transactional
    public void sendRankChangeMessage() {
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime monthStart = LocalDate.now().withDayOfMonth(1).atStartOfDay();

        // 1️⃣ 월간 랭킹 조회 (정렬 보장)
        List<RankingRowResponse> rows =
                rankingQueryRepository.getRankingRows(monthStart, now, null);

        if (rows.isEmpty()) return;

        // 2️⃣ 순위 계산 (쿼리 순서 기준)
        Map<Long, Integer> currentRankMap = new HashMap<>();
        int rank = 1;
        Long prevScore = null;

        for (int i = 0; i < rows.size(); i++) {
            RankingRowResponse row = rows.get(i);
            Number score = row.getTotalScore();
            Long scoreValue = score.longValue();

            if (i == 0 || !scoreValue.equals(prevScore)) {
                rank = i + 1;
            }

            currentRankMap.put(row.getUserId(), rank);
            log.debug(
                    "[RankCalc] userId={}, score={}, rank={}",
                    row.getUserId(), scoreValue, rank
            );
            prevScore = scoreValue;
        }

        Set<Long> userIds = currentRankMap.keySet();

        // 3️⃣ 유저 + 기존 state 조회
        Map<Long, UserEntity> userMap = userRepository.findAllById(userIds).stream()
                .collect(Collectors.toMap(UserEntity::getUserId, u -> u));

        Map<Long, RankChangeStateEntity> stateMap =
                rankChangeStateRepository.findAllById(userIds).stream()
                        .collect(Collectors.toMap(RankChangeStateEntity::getUserId, s -> s));

        List<RankChangeStateEntity> statesToSave = new ArrayList<>();

        // 4️⃣ 순위 비교
        for (RankingRowResponse row : rows) {
            Long userId = row.getUserId();
            int currentRank = currentRankMap.get(userId);

            UserEntity user = userMap.get(userId);
            if (user == null) continue;

            RankChangeStateEntity state = stateMap.get(userId);

            // 4-1️⃣ 최초 state → 저장만
            if (state == null) {
                RankChangeStateEntity newState =
                        RankChangeStateEntity.create(userId, currentRank);

                statesToSave.add(newState);
                stateMap.put(userId, newState);
                continue;
            }

            int lastRank = state.getLastCheckedRank();

            // 4-2️⃣ 순위 상승 시만 알림
            if (user.isAlertAgreed() && currentRank < lastRank) {
                try {
                    String message = messageFormatUtil.formatRankChange(
                            user.getUsername(),
                            lastRank,
                            currentRank,
                            row.getTotalScore()
                    );
                    slackMessageSender.sendMessage(user.getSlackId(), message);
                } catch (Exception e) {
                    log.warn("순위 변동 DM 실패 userId={}, err={}", userId, e.getMessage());
                }
            }

            state.updateRank(currentRank);
            statesToSave.add(state);
        }

        // 5️⃣ 상태 저장
        if (!statesToSave.isEmpty()) {
            rankChangeStateRepository.saveAll(statesToSave);
        }
    }
}
