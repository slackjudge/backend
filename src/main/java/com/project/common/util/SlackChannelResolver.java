package com.project.common.util;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class SlackChannelResolver {

    @Value("${slack.channel.daily-rank}")
    private String dailyRankChannel;

    public String dailyRank() {
        return dailyRankChannel;
    }
}
