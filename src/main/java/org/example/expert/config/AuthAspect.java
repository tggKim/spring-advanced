package org.example.expert.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Component;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;

import java.nio.charset.StandardCharsets;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Map;

@Aspect
@Component
@Slf4j
public class AuthAspect {
    @Around("execution(* org.example.expert.domain.user.controller.UserAdminController.changeUserRole(..))" +
            " || execution(* org.example.expert.domain.comment.controller.CommentAdminController.deleteComment(..))")
    public Object logRequestAndResponse(ProceedingJoinPoint joinPoint) throws  Throwable{

        DateFormat dateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");

        ServletRequestAttributes attributes = (ServletRequestAttributes)RequestContextHolder.getRequestAttributes();
        HttpServletRequest request = attributes.getRequest();
        if(request == null){
            return joinPoint.proceed();
        }
        HttpServletResponse response = attributes.getResponse();

        ContentCachingRequestWrapper wrappedRequest = (ContentCachingRequestWrapper) request;

        String requestBody = new String(wrappedRequest.getContentAsByteArray(), StandardCharsets.UTF_8);

        log.info("[AOP] 요청 시각 = {}", dateFormat.format(new Date()));
        log.info("[AOP] 사용자 ID = {}", request.getAttribute("userId"));
        log.info("[AOP] 요청 URL = {}", request.getRequestURL());
        log.info("[AOP] 요청 본문 = {}", requestBody);

        Object result = joinPoint.proceed();

        if(result == null){
            log.info("[AOP] 응답 본문이 비어있습니다.");
        }
        else{
            ResponseEntity responseEntity = (ResponseEntity) result;

            ObjectMapper objectMapper = new ObjectMapper();
            String responseString = objectMapper.writeValueAsString(responseEntity.getBody());

            log.info("[AOP] 응답 본문 = {}", responseString);
        }


        log.info("[AOP] 종료시간 = {}", dateFormat.format(new Date()));

        return result;
    }
}
