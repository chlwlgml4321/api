package kr.co.hectofinancial.mps.api.v1.common.controller;

import org.springframework.web.servlet.AsyncHandlerInterceptor;

import javax.servlet.http.HttpServletRequest;

public class BaseController implements AsyncHandlerInterceptor {



    /**
     * WAS 는 보통 2차 방화벽 안에 있고 Web Server 를 통해 client 에서 호출되거나 cluster로
     * 구성되어 load balancer 에서 호출되는데
     * 이럴 경우에서 getRemoteAddr() 을 호출하면 웹서버나 load balancer의 IP 가 나옴
     * 위와 같은 문제를 해결하기 위해 사용되는 HTTP Header인 X-Forwarded-For 값을 확인해서 있으면
     * 해당 키값을 사용하고 없으면 getRemoteAddr()사용
     */
    protected String getRemoteAddr(HttpServletRequest request){
        return (null != request.getHeader("X-FORWARDED-FOR")) ? request.getHeader("X-FORWARDED-FOR") : request.getRemoteAddr();
    }
}
