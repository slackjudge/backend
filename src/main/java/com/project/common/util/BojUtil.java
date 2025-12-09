package com.project.common.util;

import com.google.gson.Gson;
import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BojUtil {

    private final Gson gson = new Gson();

    public boolean checkBojId(String baekjoonId) {
        Map<String, Object> map = getBojMap(baekjoonId);

        Double count = (Double) map.get("count");

        return count != 0;
    }

    public int getBojTier(String baekjoonId) {
        Map<String, Object> map = getBojMap(baekjoonId);

        List<Map<String, Object>> items = (List<Map<String, Object>>) map.get("items");
        Map<String, Object> userData = items.get(0);

        Double tierDouble = (Double) userData.get("tier");
        return tierDouble.intValue();
    }

    private Map<String, Object> getBojMap(String baekjoonId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://solved.ac/api/v3/search/user?query=" + baekjoonId))
                .header("x-solvedac-language", "ko")
                .header("Accept", "application/json")
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = HttpClient.newHttpClient().send(request, HttpResponse.BodyHandlers.ofString());
        } catch (IOException | InterruptedException e) {
            if (e instanceof InterruptedException) {
                Thread.currentThread().interrupt();
            }
            throw new BusinessException(ErrorCode.BAEKJOON_AUTH_INVALID);
        }

        return gson.fromJson(response.body(), Map.class);
    }
}
