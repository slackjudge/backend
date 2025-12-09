package com.project.common.util;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.dto.response.SlackTokenResponse;
import com.project.dto.response.SlackUserInfoResponse;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.HttpEntity;
import org.springframework.http.HttpMethod;
import org.springframework.http.ResponseEntity;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.util.LinkedMultiValueMap;
import org.springframework.util.MultiValueMap;
import org.springframework.web.client.RestTemplate;

import java.util.Collections;

import static com.project.common.util.AuthConstants.AUTHORIZATION;
import static com.project.common.util.AuthConstants.TOKEN_PREFIX;

@Component
public class SlackUtil {

    private final RestTemplate restTemplate = new RestTemplate();

    @Value("${oauth.slack.client-id}")
    private String slackClientId;

    @Value("${oauth.slack.client-secret}")
    private String slackClientSecret;

    @Value("${oauth.slack.redirect-uri}")
    private String slackRedirectUri;

    public SlackTokenResponse getSlackToken(String code) {
        String requestUrl = "https://slack.com/api/openid.connect.token";

        MultiValueMap<String, String> parameters = getParameters(code);
        HttpHeaders headers = getTokenHeaders();

        HttpEntity<MultiValueMap<String, String>> httpEntity = new HttpEntity<>(parameters, headers);
        SlackTokenResponse slackTokenResponse = restTemplate.postForObject(requestUrl, httpEntity, SlackTokenResponse.class);

        if (slackTokenResponse == null || !slackTokenResponse.ok()) {
            throw new BusinessException(ErrorCode.SLACK_AUTHENTICATION_FAILED);
        }

        return slackTokenResponse;
    }

    public SlackUserInfoResponse getSlackUserInfo(String accessToken) {
        String url = "https://slack.com/api/openid.connect.userInfo";

        HttpHeaders headers = getUserInfoHeaders(accessToken);

        HttpEntity<Void> entity = new HttpEntity<>(headers);

        ResponseEntity<SlackUserInfoResponse> response =
                restTemplate.exchange(url, HttpMethod.GET, entity, SlackUserInfoResponse.class);

        return response.getBody();
    }

    private HttpHeaders getUserInfoHeaders(String accessToken) {
        HttpHeaders headers = new HttpHeaders();
        headers.set(AUTHORIZATION.getValue(), TOKEN_PREFIX.getValue() + accessToken);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));

        return headers;
    }

    private HttpHeaders getTokenHeaders() {
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_FORM_URLENCODED);
        headers.setAccept(Collections.singletonList(MediaType.APPLICATION_JSON));
        return headers;
    }

    private MultiValueMap<String, String> getParameters(String code) {
        MultiValueMap<String, String> parameters = new LinkedMultiValueMap<>();
        parameters.add("client_id", slackClientId);
        parameters.add("client_secret", slackClientSecret);
        parameters.add("redirect_uri", slackRedirectUri);
        parameters.add("code", code);

        return parameters;
    }
}
