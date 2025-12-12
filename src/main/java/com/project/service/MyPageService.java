package com.project.service;

import com.project.common.exception.BusinessException;
import com.project.common.exception.ErrorCode;
import com.project.dto.response.MyPageResponse;
import com.project.entity.UserEntity;
import com.project.repository.MyPageRepository;
import com.project.repository.RankingDayRepository;
import com.project.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.DateTimeException;
import java.time.LocalDate;
import java.util.List;

@Service
@RequiredArgsConstructor
@Transactional(readOnly = true) //조회 전용 트랜잭션 ( 성능 최적화)
public class MyPageService {

    private final UserRepository userRepository;
    private final MyPageRepository myPageRepository; //Querydsl
    private final RankingDayRepository rankingDayRepository; //Native SQL

    public MyPageResponse getMyPageData(Long userId, int year, int month, String dateStr) {
        //1. 유저 조회
        UserEntity user = userRepository.findById(userId)
                .orElseThrow(() -> new BusinessException(ErrorCode.USER_NOT_FOUND));
        //2. 기준 날짜 설정 & 유효성 검증
        LocalDate targetDate = determinTargetDate(year, month, dateStr);

        //3. 잔디: 월간 데이터 조회
        List<MyPageResponse.Grass> grassList = myPageRepository.findGrassList(userId, year, month);

        //4. 상세: 일간 문제 목록 조회 (푼 시간 정렬)
        List<MyPageResponse.Problem> problemList = myPageRepository.findSolvedProblemsByDate(userId, targetDate);

        //5. 통계 계산
        int solvedCount = problemList.size();

        int dailyScore = problemList.stream()
                .mapToInt(MyPageResponse.Problem::getTierLevel)
                .sum();

        int maxDifficulty = problemList.stream()
                .mapToInt(MyPageResponse.Problem::getTierLevel)
                .max().orElse(0);


        //6. 상세: 일간 랭킹 계산(Native Query)
        //문제를 풀었을때만 랭킹 계산
        long dailyRank = 0;
        if (solvedCount > 0) {
            dailyRank = rankingDayRepository.calculateDailyRank(
                    dailyScore,
                    targetDate.atStartOfDay(),
                    targetDate.atTime(23, 59, 59)
            );
        }

        //7. 전체 랭킹 및 정조
        int myTotalScore = user.getTotalSolvedCount() != null ? user.getTotalSolvedCount() : 0;

        // 8. DTO 조립 및 변환
        return buildResponse(user, myTotalScore, targetDate, dailyScore, (int) dailyRank, solvedCount, maxDifficulty, problemList, grassList);
    }
    /**
     * 날짜 결정 로직
     * 1. dateStr 있음 -> 해당 날짜
     * 2. dateStr 없음 & 이번 달 조회 -> 오늘 날짜
     * 3. dateStr 없음 & 다른 달 조회 -> 해당 월 1일
     */
     private LocalDate determinTargetDate(int year, int month, String dateStr) {
         try {
             if (dateStr != null && !dateStr.isBlank()) {
                 return LocalDate.parse(dateStr);
             }
             LocalDate today = LocalDate.now();
             if (year == today.getYear() && month == today.getMonthValue()) {
                 return today; //이번 달이면 오늘 보여줌
             } else {
                 return LocalDate.of(year, month, 1); //다른 날이면 1일 보여줌
             }
         }catch(DateTimeException e) {
             throw new BusinessException(ErrorCode.INVALID_INPUT, "날짜 형식이 올바르지 않습니다.  ");
         }
     }
     /**
      * DTO 빌더
      */
     private MyPageResponse buildResponse(UserEntity user, int totalScore,LocalDate date,
                                          int dailyScore, int dailyRank, int solvedCount, int maxDifficulty,
                                          List<MyPageResponse.Problem> problemList, List<MyPageResponse.Grass> grassList) {
         MyPageResponse.Profile profile = MyPageResponse.Profile.builder()
                 .username(user.getUsername())
                 .baekjoonId(user.getBaekjoonId())
                 .tierLevel(user.getBojTier())
                 .totalScore((long) totalScore)
                 .build();
         MyPageResponse.SelectedDateDetail detail = MyPageResponse.SelectedDateDetail.builder()
                 .date(date.toString())
                 .dailyScore(dailyScore)
                 .dailyRank(dailyRank)
                 .solvedCount(solvedCount)
                 .maxDifficulty(maxDifficulty)
                 .problems(problemList)
                 .build();
         return MyPageResponse.builder()
                 .profile(profile)
                 .grass(grassList)
                 .selectedDateDetail(detail)
                 .build();
     }


}
