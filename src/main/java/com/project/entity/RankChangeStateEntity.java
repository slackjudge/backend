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
    @Column(name = "user_id")
    private Long userId;

    @Column(name = "last_checked_rank", nullable = false)
    private int lastCheckedRank;

    private RankChangeStateEntity(Long userId, int lastCheckedRank) {
        this.userId = userId;
        this.lastCheckedRank = lastCheckedRank;
    }

    // 최초 생성 시 알림은 보내지 않고 기준만 저장한다.
    public static RankChangeStateEntity create(Long userId, int lastCheckedRank) {
        return new RankChangeStateEntity(userId, lastCheckedRank);
    }

    public void updateRank(int newRank) {
        this.lastCheckedRank = newRank;
    }
}
