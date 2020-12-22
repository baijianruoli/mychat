package com.zut.lpf.controller;

import com.zut.lpf.entity.MessageEntity;
import com.zut.lpf.response.BaseResponse;
import com.zut.lpf.response.StatusCode;
import com.zut.lpf.service.MsgService;
import com.zut.lpf.service.UserService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiImplicitParams;
import io.swagger.annotations.ApiOperation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Api("page")
@RequestMapping("/page")
@RestController
public class PageController {

    @Autowired
    private MsgService msgService;

    @ApiOperation(value = "分页请求")
    @ApiImplicitParams({
            @ApiImplicitParam(name = "current", value = "当前页"),
            @ApiImplicitParam(name = "name", value = "本人用户名"),
            @ApiImplicitParam(name = "friendName", value = "好友用户名")
    })
    //分页加载
    @GetMapping("/get")
    public BaseResponse getPage(int current, String name, String friendName) {
        System.out.println(current);
        List<MessageEntity> list;
        if ("all".equals(friendName)) {
            list = msgService.getAll(current);
        } else {
            list = msgService.getList(name, friendName, current);
        }
        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
        baseResponse.setData(list);
        return baseResponse;
    }
}
