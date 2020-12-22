package com.zut.lpf.exception;


import com.zut.lpf.response.BaseResponse;
import com.zut.lpf.response.StatusCode;
import lombok.extern.slf4j.Slf4j;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.MethodArgumentNotValidException;
import org.springframework.web.bind.annotation.ExceptionHandler;
import org.springframework.web.bind.annotation.RestControllerAdvice;

import java.util.HashMap;
import java.util.Map;

//全局异常捕获
@Slf4j
@RestControllerAdvice(basePackages = "com.zut.lpf.controller")
public class GulimallExceptionController {

    @ExceptionHandler(value = Exception.class)
    public BaseResponse handleException(Exception e) {
        BaseResponse baseResponse = new BaseResponse(StatusCode.Fail);
        baseResponse.setData(e.getMessage());
        return baseResponse;
    }

}
