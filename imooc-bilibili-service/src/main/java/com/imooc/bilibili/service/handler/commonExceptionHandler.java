package com.imooc.bilibili.service.handler;

import com.imooc.bilibili.domain.JsonResponse;
import com.imooc.bilibili.domain.exception.ConditionException;
import org.springframework.core.Ordered;
import org.springframework.core.annotation.Order;
import org.springframework.web.bind.annotation.ControllerAdvice;

import javax.servlet.http.HttpServletRequest;

@ControllerAdvice
@Order(Ordered.HIGHEST_PRECEDENCE)
public class commonExceptionHandler {
    public JsonResponse<String> commonExceptionHandler(HttpServletRequest request,Exception e){
        String message = e.getMessage();
        if(e instanceof ConditionException){
            String code = ((ConditionException) e).getCode();
            return new JsonResponse<>(code,message);
        }else {
            return new JsonResponse<>("500",message);
        }
    }
}
