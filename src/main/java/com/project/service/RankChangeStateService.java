package com.project.service;

import com.project.common.util.MessageFormatUtil;
import com.project.common.util.RankUtil;
import com.project.common.util.SlackMessageSender;
import com.project.dto.response.RankingRowResponse;
import com.project.entity.UserEntity;
import com.project.repository.RankingQueryRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.Clock;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author 김경민
 */
@Service
@Slf4j
@RequiredArgsConstructor
public class RankChangeStateService {

    private final SlackMessageSender slackMessageSender;
    private final MessageFormatUtil messageFormatUtil;
    private final UserRepository userRepository;
    private final RankingQueryRepository rankingQueryRepository;
    private final Clock clock;

    /**==========================
    *
    * 사용자 순위 변동을 계산하여 Slack DM 알림을 전송한다.
    *
    * @parm -
    * @return void
    * @author 김경민
    * @version 1.0.0
    * @date 2025-12-14
    *
    ==========================**/
    @Transactional
    public void sendRankChangeMessage() {
        LocalDateTime now = LocalDateTime.now(clock);

        LocalDateTime baseTime = RankUtil.resolveBaseTime(now);
        LocalDateTime periodStart =
                RankUtil.getPeriodStart("day", baseTime);
        LocalDateTime currentEnd =
                RankUtil.getPeriodEndInclusive("day", baseTime, now);
        LocalDateTime prevEnd =
                currentEnd.minusHours(1);

        // 현재 / 이전 랭킹 조회
        List<RankingRowResponse> current =
                rankingQueryRepository.getRankingRows(periodStart, currentEnd, null);

        if (current.isEmpty()) {
            log.info("[RankChange] no ranking data");
            return;
        }

        List<RankingRowResponse> prev =
                rankingQueryRepository.getRankingRows(periodStart, prevEnd, null);

        // 순위 계산
        calculateRanks(current);
        calculateRanks(prev);
        calculateDiff(current, prev);

        Map<Long, UserEntity> userMap =
                userRepository.findAllById(
                        current.stream()
                                .map(RankingRowResponse::getUserId)
                                .collect(Collectors.toSet())
                ).stream().collect(Collectors.toMap(UserEntity::getUserId, u -> u));

        // 알림 발송
        for (RankingRowResponse row : current) {

            if (row.getDiff() <= 0) continue;

            UserEntity user = userMap.get(row.getUserId());
            if (user == null || !user.isAlertAgreed()) continue;

            try {
                String message = messageFormatUtil.formatRankChange(
                        user.getUsername(),
                        row.getRank() + row.getDiff(),
                        row.getRank(),
                        row.getTotalScore()
                );

                slackMessageSender.sendMessage(user.getSlackId(), message);

                log.info(
                        "[RankChange] DM sent userId={}, prevRank={}, currentRank={}",
                        user.getUserId(),
                        row.getRank() + row.getDiff(),
                        row.getRank()
                );

            } catch (Exception e) {
                log.warn(
                        "[RankChange] DM failed userId={}, err={}",
                        user.getUserId(),
                        e.getMessage()
                );
            }
        }
    }

    private void calculateRanks(List<RankingRowResponse> rows) {

        if (rows.isEmpty()) return;

        rows.get(0).setRank(1);

        for (int i = 1; i < rows.size(); i++) {
            RankingRowResponse prev = rows.get(i - 1);
            RankingRowResponse curr = rows.get(i);

            if (prev.getTotalScore() == curr.getTotalScore()) {
                curr.setRank(prev.getRank());
            } else {
                curr.setRank(i + 1);
            }
        }
    }
    private void calculateDiff(
            List<RankingRowResponse> current,
            List<RankingRowResponse> previous
    ) {

        Map<Long, RankingRowResponse> prevByUserId =
                previous.stream().collect(Collectors.toMap(
                        RankingRowResponse::getUserId,
                        r -> r
                ));

        for (RankingRowResponse cur : current) {
            RankingRowResponse prev = prevByUserId.get(cur.getUserId());

            if (prev == null || prev.getRank() == 0) {
                cur.setDiff(0);
            } else {
                cur.setDiff(prev.getRank() - cur.getRank());
            }
        }
    }
}
