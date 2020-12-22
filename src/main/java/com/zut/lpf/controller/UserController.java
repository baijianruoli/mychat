package com.zut.lpf.controller;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zut.lpf.dao.UserDao;
import com.zut.lpf.entity.UserEntity;
import com.zut.lpf.response.BaseResponse;
import com.zut.lpf.response.StatusCode;
import com.zut.lpf.service.ExecutorChannel;
import com.zut.lpf.service.RedisService;
import com.zut.lpf.service.UserService;
import com.zut.lpf.util.GlobalCode;
import com.zut.lpf.util.GlobalLock;
import com.zut.lpf.vo.UserVo;
import io.swagger.annotations.Api;
import io.swagger.annotations.ApiImplicitParam;
import io.swagger.annotations.ApiOperation;
import io.swagger.annotations.ApiParam;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.annotation.CacheEvict;
import org.springframework.cache.annotation.Cacheable;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.util.DigestUtils;
import org.springframework.web.bind.annotation.*;

import javax.annotation.PostConstruct;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.stream.Collectors;


@Api("基础服务")
@RestController
@Slf4j
public class UserController {
    @Autowired
    private UserDao userDao;
    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ExecutorChannel executorChannel;
    @Autowired
    private UserService userService;

    //登录
    @ApiOperation("登录接口")
    @PostMapping("/login")
    public BaseResponse login(@RequestBody @ApiParam("用户信息") UserEntity userEntity) throws InterruptedException {
        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
        QueryWrapper<UserEntity> wrapper = new QueryWrapper<UserEntity>().eq("name", userEntity.getName());
        UserEntity loginEntity = userDao.selectOne(wrapper);
        if (loginEntity == null) {
            baseResponse.setMsg("账号不存在");
            baseResponse.setCode(GlobalCode.CODE_OK);
            return baseResponse;

        } else {
            if (DigestUtils.md5DigestAsHex(userEntity.getPassword().getBytes()).equals(loginEntity.getPassword())) {
                //http等待websocket执行完毕
                GlobalLock.HttpLock.acquire();
                //创建消息队列
                redisService.createQueue(loginEntity.getName());
                //redis查询好友
                List<UserEntity> userEntities = redisService.redisFindFriendList(userEntity.getName());
                loginEntity.setFriendList(userEntities);
                GlobalLock.humanToChannelId.put(loginEntity.getName(), GlobalLock.remoteId);

                GlobalLock.flag.put(GlobalLock.remoteId, GlobalCode.CODE_OK);

                loginEntity.setRemoteId(GlobalLock.remoteId);
                //更新最后访问时间
                userService.updateVisitTime(loginEntity);
                //加入Redis的Uv记录
                redisService.UvLoad(loginEntity.getId());
                //获取历史信息
                UserVo userVo = userService.getOthers(loginEntity);
                baseResponse.setData(userVo);
                //创建线程任务
                executorChannel.executorSumbit(userEntity.getName());
                //http执行完毕，netty信号量加一
                GlobalLock.nettyLock.release();
                return baseResponse;
            } else {
                baseResponse.setMsg("密码不正确");
                baseResponse.setCode(GlobalCode.CODE_OK);
                return baseResponse;
            }
        }

    }

    //注册
    @ApiOperation("注册接口")
    @PostMapping("/register")
    @CacheEvict(value = "MychatUser", allEntries=true)
    public BaseResponse register(@RequestBody UserEntity userEntity) {
        userEntity.setIcon("https://www.mzyyun.com/api/img.php");
        userEntity.setCreateTime(new Date());
        userEntity.setLastVisit(new Date());
        String s = DigestUtils.md5DigestAsHex(userEntity.getPassword().getBytes());
        userEntity.setPassword(s);
        try {
            userDao.insert(userEntity);
        } catch (Exception e) {
            e.printStackTrace();
            BaseResponse baseResponse = new BaseResponse(StatusCode.Fail);
            baseResponse.setData(e.getMessage());
            return baseResponse;
        }
        return new BaseResponse(StatusCode.Success);
    }

    //模糊搜索用户
    @ApiOperation("模糊搜索用户")
    @GetMapping("/searchByName")
    @Cacheable(value = "MychatUser")
    public BaseResponse searchByName(String searchName, String name) {
        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
        List<UserEntity> list = userDao.selectList(new QueryWrapper<UserEntity>().like("name", searchName));
        List<UserEntity> friendList = redisService.redisFindFriendList(name);
        HashMap<String, Integer> hashMap = new HashMap<>();
        friendList.stream().forEach(res -> {
            hashMap.put(res.getName(), GlobalCode.CODE_OK);
        });
        hashMap.put(name, GlobalCode.CODE_OK);
        List<UserEntity> collect = list.stream().filter(res -> !hashMap.containsKey(res.getName())).collect(Collectors.toList());
        if (collect != null) {
            baseResponse.setData(collect);
            return baseResponse;
        } else {
            baseResponse.setMsg("无数据");
            return baseResponse;
        }
    }

    //更新
    @ApiOperation("更新用户信息")
    @PostMapping("/update")
    public BaseResponse update(@RequestBody UserEntity userEntity) {
        userDao.updateById(userEntity);
        return new BaseResponse(StatusCode.Success);
    }

    //是否在线
    @ApiOperation("是否在线")
    @ApiImplicitParam(name = "name", paramType = "query")
    @GetMapping("/manOnline")
    public BaseResponse manOline(@ApiParam(value = "本人用户名") String name) {
        BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
        if (GlobalLock.humanToChannelId.containsKey(name) && GlobalLock.flag.containsKey(GlobalLock.humanToChannelId.get(name))) {
            baseResponse.setData(GlobalCode.CODE_OK);
        } else {
            baseResponse.setData(GlobalCode.CODE_FAIL);
        }
        return baseResponse;
    }

    @ApiOperation("退出系统")
    @GetMapping("/logout")
    public BaseResponse logout(@ApiParam("本人用户名") String name) {
        String s = GlobalLock.humanToChannelId.get(name);
        GlobalLock.flag.remove(s);
        log.info("{}退出", name);
        return new BaseResponse(StatusCode.Success);
    }

}
