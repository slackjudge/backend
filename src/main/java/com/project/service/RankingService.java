package com.project.service;

import com.project.repository.RankingQueryRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class RankingService {

  private final RankingQueryRepository rankingQueryRepository;
}
