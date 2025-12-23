package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.common.util.BojUtil;
import com.project.dto.request.SignUpRequest;
import com.project.dto.response.BojCheckResponse;
import com.project.entity.UserEntity;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
@RequiredArgsConstructor
public class UserService {

    private final UserRepository userRepository;
    private final BojUtil bojUtil;

    @Transactional
    public UserEntity findUser(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
    }

    @Transactional
    public UserEntity findUserBySlackId(String slackId) {
        return userRepository.findBySlackId(slackId)
                .orElseGet(() -> userRepository.save(UserEntity.createUser(slackId)));
    }

    @Transactional
    public void signUp(Long userId, SignUpRequest signUpRequest) {

        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        int bojTier = bojUtil.getBojTier(signUpRequest.baekjoonId());

        user.signUp(signUpRequest, bojTier);
        userRepository.save(user);
    }

    @Transactional
    public BojCheckResponse checkBaekjoonId(Long userId, String baekjoonId) {

        // User Not Found 예외처리
        userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));

        boolean isUsed = userRepository.existsByBaekjoonId(baekjoonId);
        boolean isBaekjoonId = bojUtil.checkBojId(baekjoonId);

        return new BojCheckResponse(
                baekjoonId,
                isBaekjoonId,
                isUsed
        );
    }
}
