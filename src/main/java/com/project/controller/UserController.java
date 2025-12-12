package com.project.controller;


import com.project.common.dto.ApiResponse;
import com.project.common.security.SecurityUserDetails;
import com.project.dto.request.SignUpRequest;
import com.project.dto.response.BojCheckResponse;
import com.project.dto.response.MyPageResponse;
import com.project.service.MyPageService;
import com.project.service.UserService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@RestController
@RequiredArgsConstructor
@RequestMapping("/user")
@Slf4j
public class UserController {

    private final UserService userService;
    private final MyPageService myPageService;

    @GetMapping("/check")
    public ResponseEntity<ApiResponse<BojCheckResponse>> checkUser(@AuthenticationPrincipal SecurityUserDetails userDetails,
                                                                   @RequestParam String baekjoonId) {
        return ResponseEntity.ok(ApiResponse.success(
                "백준 회원 체크 성공", userService.checkBaekjoonId(userDetails.getId(), baekjoonId)));
    }

    @PostMapping("/signUp")
    public ResponseEntity<ApiResponse<String>> signUp(@AuthenticationPrincipal SecurityUserDetails userInfo,
                                                      @RequestBody SignUpRequest signUpRequest) {
        userService.signUp(userInfo.getId(), signUpRequest);
        return ResponseEntity.ok(ApiResponse.success("회원가입 성공", null));
    }

    @GetMapping("/me")
    public ResponseEntity<ApiResponse<MyPageResponse>> getMyPage(
            @AuthenticationPrincipal SecurityUserDetails userDetails,
            @RequestParam int year,
            @RequestParam int month,
            @RequestParam(required = false) String date
    ) {
        //userDetails.getId()를 통해 현재 로그인한 유저 Pk를 가져와 넘긴다.
        MyPageResponse response = myPageService.getMyPageData(userDetails.getId(), year, month, date);
        return ResponseEntity.ok(ApiResponse.success("마이페이지 조회 성공", response));
    }
}
