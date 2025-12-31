package com.project.config.log;

import com.project.common.util.AspectUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.AfterThrowing;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

@Slf4j
@Aspect
@Component
@RequiredArgsConstructor
public class AspectConfig {

    private final AspectUtil aspectUtil;

    @AfterReturning(
            value = "com.project.config.log.PointCuts.allController()",
            returning = "result"
    )
    public void doReturn(JoinPoint joinPoint, Object result) {
        aspectUtil.putCommon(joinPoint);
        aspectUtil.putSuccess(result);

        log.info("Request Success");

        aspectUtil.clear();
    }

    @AfterThrowing(
            value = "com.project.config.log.PointCuts.allController()",
            throwing = "ex"
    )
    public void doThrowing(JoinPoint joinPoint, Throwable ex) {
        aspectUtil.putCommon(joinPoint);
        aspectUtil.putError(ex);

        log.error("Request Failed");

        aspectUtil.clear();
    }
}
