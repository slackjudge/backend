package com.project.common.util;

import com.google.gson.Gson;
import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.time.Duration;

import com.project.dto.response.BojUserResponse;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class BojUtil {

    private final Gson gson;
    private final HttpClient httpClient;

    public BojUtil(Gson gson) {
        this.gson = gson;

        this.httpClient =
                HttpClient.newBuilder()
                        .version(HttpClient.Version.HTTP_2)
                        .connectTimeout(Duration.ofSeconds(3))
                        .build();
    }

    public boolean checkBojId(String baekjoonId) {
        BojUserResponse response = requestBojUser(baekjoonId);
        return response != null;
    }

    public int getBojTier(String baekjoonId) {
        BojUserResponse response = requestBojUser(baekjoonId);
        if (response == null) {
            throw new BusinessException(ErrorCode.BAEKJOON_AUTH_INVALID);
        }
        return response.tier();
    }

    private BojUserResponse requestBojUser(String baekjoonId) {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://solved.ac/api/v3/user/show?handle=" + baekjoonId))
                .header("x-solvedac-language", "ko")
                .header("Accept", "application/json")
                .timeout(Duration.ofSeconds(5))
                .GET()
                .build();

        try {
            HttpResponse<String> response =
                    httpClient.send(request, HttpResponse.BodyHandlers.ofString());

            if (response.statusCode() == 404) {
                return null;
            }

            if (response.statusCode() != 200) {
                throw new BusinessException(ErrorCode.BAEKJOON_AUTH_INVALID);
            }

            return gson.fromJson(response.body(), BojUserResponse.class);

        } catch (InterruptedException e) {
            Thread.currentThread().interrupt();
            throw new BusinessException(ErrorCode.BAEKJOON_AUTH_INVALID);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.BAEKJOON_AUTH_INVALID);
        }
    }
}
