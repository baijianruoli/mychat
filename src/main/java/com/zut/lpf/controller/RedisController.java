package com.zut.lpf.controller;

import com.zut.lpf.entity.UserEntity;
import com.zut.lpf.response.BaseResponse;
import com.zut.lpf.response.StatusCode;
import com.zut.lpf.service.RedisService;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@Api("好友接口")
public class RedisController {

    @Autowired
    private RedisService redisService;


    //添加好友
    @ApiOperation("添加好友")
    @GetMapping("/addFriend")
    public BaseResponse addFriend(@ApiParam("好友用户名") String friendName, @ApiParam("本人用户名") String name) {
        redisService.redisAddFriend(friendName, name);
        return new BaseResponse(StatusCode.Success);
    }

    //获取好友列表
    @ApiOperation("获取好友列表")
    @GetMapping("/getFriendList")
    public BaseResponse addFriend(@ApiParam("本人用户名") String name) {

        List<UserEntity> list = redisService.redisFindFriendList(name);
        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
        baseResponse.setData(list);
        return baseResponse;
    }

    @ApiOperation("删除好友")
    //删除好友
    @GetMapping("/removeFriend")
    public BaseResponse removeFriend(@ApiParam("本人用户名") String name, @ApiParam("好友用户名") String friendName) {
        redisService.redisRemoveFriend(friendName, name);
        return new BaseResponse(StatusCode.Success);
    }
}
