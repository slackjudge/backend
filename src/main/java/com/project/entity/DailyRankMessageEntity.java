package com.project.entity;

import com.project.common.BaseTimeEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Id;
import jakarta.persistence.Table;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

/**
 * @author 김경민
 */
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

    private DailyRankMessageEntity(String messageContent) {
        this.messageContent = messageContent;
    }
    public static DailyRankMessageEntity of(String messageContent) {
        return new DailyRankMessageEntity(messageContent);
    }
}
