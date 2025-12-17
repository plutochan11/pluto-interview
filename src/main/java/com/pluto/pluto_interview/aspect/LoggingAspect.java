package com.pluto.pluto_interview.aspect;

import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.JoinPoint;
import org.aspectj.lang.annotation.After;
import org.aspectj.lang.annotation.AfterReturning;
import org.aspectj.lang.annotation.Aspect;
import org.aspectj.lang.annotation.Pointcut;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.Objects;
import java.util.stream.Collectors;

@Aspect
//@Component
@Slf4j
public class LoggingAspect {
	@Pointcut("@annotation(com.pluto.pluto_interview.annotation.Log)")
	public void logAnnotationPointCut() {}

	@AfterReturning(pointcut = "logAnnotationPointcut()")
	public void afterReturningLog(JoinPoint joinPoint) {
		String methodName = joinPoint.getSignature().toShortString();
		String arguments = Arrays.stream(joinPoint.getArgs())
			  .map(Objects::toString)
			  .collect(Collectors.joining(", "));
		log.info("Method {} executed successfully with arguments: {}", methodName, arguments);
	}

}
