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
import java.time.Duration;
import java.util.List;
import java.util.Map;

@Slf4j
@Component
public class BojUtil {

    private final Gson gson;
    private final HttpClient httpClient;

    public BojUtil(Gson gson) {
        this.gson = gson;

        this.httpClient = HttpClient.newBuilder()
                .version(HttpClient.Version.HTTP_2)
                .connectTimeout(Duration.ofSeconds(3)) // 연결 지연 대비 (3초 추천)
                .build();

    }

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
                .timeout(Duration.ofSeconds(10)) // solved api는 응답이 매우 빠르므로 10초로 두었습니다
                .GET()
                .build();

        HttpResponse<String> response;
        try {
            response = httpClient.send(request, HttpResponse.BodyHandlers.ofString());
        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.BAEKJOON_AUTH_INVALID);

        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAEKJOON_AUTH_INVALID);
        }

        return gson.fromJson(response.body(), Map.class);
    }
}
