package com.zut.lpf.controller;

import com.zut.lpf.response.BaseResponse;
import com.zut.lpf.response.StatusCode;
import com.zut.lpf.service.RedisService;
import com.zut.lpf.vo.UvVo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/echarts")
public class EchartsController {
    @Autowired
    private RedisService redisService;

    //获取Uv数据列表
    @GetMapping("/getList")
    public BaseResponse getList() {
        List<UvVo> hyperLog = redisService.getHyperLog();
        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
        baseResponse.setData(hyperLog);
        return baseResponse;
    }
}
