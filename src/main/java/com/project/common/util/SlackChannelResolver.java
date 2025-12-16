package com.project.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SlackChannelResolver {

    private final String dailyRankChannel;
    public SlackChannelResolver(@Value("${slack.channel.daily-rank}") String dailyRankChannel) {
        this.dailyRankChannel = dailyRankChannel;
    }
    public String dailyRank() {
        return dailyRankChannel;
    }
}
