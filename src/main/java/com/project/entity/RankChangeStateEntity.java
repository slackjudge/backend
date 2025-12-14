package com.project.entity;

import com.project.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "rank_change_state")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class RankChangeStateEntity extends BaseTimeEntity {

    @Id
    private Long userId;

    @Column(name = "last_notified_rank", nullable = false)
    private int lastNotifiedRank;

    private RankChangeStateEntity(Long userId, int lastNotifiedRank) {
        this.userId = userId;
        this.lastNotifiedRank = lastNotifiedRank;
    }

    // 최초 생성 시 알림은 보내지 않고 기준만 저장한다.
    public static RankChangeStateEntity create(Long userId, int lastNotifiedRank) {
        return new RankChangeStateEntity(userId, lastNotifiedRank);
    }

    public void updateRank(int newRank) {
        this.lastNotifiedRank = newRank;
    }
}
