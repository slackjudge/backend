package com.project.entity;

import com.project.common.BaseTimeEntity;
import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "daily_rank_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DailyRankMessageEntity extends BaseTimeEntity {

    @Id
    @Column(name = "daily_rank_message_id")
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long messageId;

    @Column(name = "message_content", columnDefinition = "TEXT", nullable = false)
    private String messageContent;

    public static DailyRankMessageEntity of (String messageContent) {
        DailyRankMessageEntity entity = new DailyRankMessageEntity();
        entity.messageContent = messageContent;
        return entity;
    }
}
