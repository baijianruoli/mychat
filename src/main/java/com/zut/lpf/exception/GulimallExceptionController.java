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

@Slf4j
@RestControllerAdvice(basePackages = "com.zut.lpf.controller")
public class GulimallExceptionController {

    @ExceptionHandler(value = Exception.class)
    public BaseResponse handleException(MethodArgumentNotValidException e)
    {

        BindingResult bindingResult = e.getBindingResult();
        Map<String,String> map=new HashMap<>();
        if(bindingResult.hasErrors())
        {

            bindingResult.getFieldErrors().forEach(res->{
                String msg=res.getDefaultMessage();
                String field=res.getField();
                map.put(field,msg);
            });

        }
       BaseResponse baseResponse= new BaseResponse(StatusCode.Fail);
        baseResponse.setData(map);
        return baseResponse;
    }

}
