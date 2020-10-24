package com.zut.lpf.controller;

import com.alibaba.fastjson.JSON;
import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zut.lpf.dao.UserDao;
import com.zut.lpf.entity.MsgEntity;
import com.zut.lpf.entity.UserEntity;
import com.zut.lpf.nettys.MyTextWebSocketHandler;
import com.zut.lpf.response.BaseResponse;
import com.zut.lpf.response.StatusCode;
import com.zut.lpf.service.ExecutorChannel;
import com.zut.lpf.service.RedisService;
import com.zut.lpf.util.GlobalLock;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.amqp.core.*;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.BoundListOperations;
import org.springframework.data.redis.core.ListOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.stream.Collectors;


@RestController
public class UserController {
    @Autowired
    private UserDao userDao;
    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private ExecutorChannel executorChannel;
        //登录
        @RequestMapping("/login")
        public BaseResponse login(@RequestBody  UserEntity userEntity, HttpServletRequest httpServletRequest) throws InterruptedException {
            BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
           QueryWrapper<UserEntity> wrapper = new QueryWrapper<UserEntity>().eq("name", userEntity.getName());
            UserEntity loginEntity = userDao.selectOne(wrapper);
              if(loginEntity==null)
              {
                  baseResponse.setMsg("账号不存在");
                  baseResponse.setCode(1);
                  return baseResponse;
              }
              else
              {
                  if(loginEntity.getPassword().equals(userEntity.getPassword()))
                  {
                      //http等待websocket执行完毕
                      GlobalLock.HttpLock.acquire();
                      if(!redisTemplate.boundHashOps("mycat").hasKey(loginEntity.getName()))
                      {
                          redisTemplate.boundHashOps("mycat").put(loginEntity.getName(),1);
                          amqpAdmin.declareQueue(new Queue(loginEntity.getName()));
                          amqpAdmin.declareExchange(new TopicExchange("mycat-topic"));
                          amqpAdmin.declareExchange(new FanoutExchange("mycat-fanout"));
                          amqpAdmin.declareBinding(new Binding(loginEntity.getName(),Binding.DestinationType.QUEUE,"mycat-fanout","kkk",null));
                          amqpAdmin.declareBinding(new Binding(loginEntity.getName(),Binding.DestinationType.QUEUE,"mycat-topic",loginEntity.getName(),null));
                      }
                      //redis查询好友
                      List<UserEntity> userEntities = redisService.redisFindFriendList(userEntity.getName());
                      loginEntity.setFriendList(userEntities);
                      GlobalLock.humanToChannelId.put(loginEntity.getName(),GlobalLock.remoteId);
                      GlobalLock.flag.put(GlobalLock.remoteId,1);
                      loginEntity.setRemoteId(GlobalLock.remoteId);
                      baseResponse.setData(loginEntity);
                      //创建线程任务
                      executorChannel.executorSumbit(userEntity.getName());
                      //http执行完毕，netty信号量加一
                      GlobalLock.nettyLock.release();
                      return baseResponse;
                  }
                  else
                  {
                      baseResponse.setMsg("密码不正确");
                      baseResponse.setCode(1);
                      return baseResponse;
                  }
              }

        }
        //注册
        @RequestMapping("/register")
        public BaseResponse register(@RequestBody  UserEntity userEntity)
        {
            userEntity.setIcon("https://liqiqip.oss-cn-beijing.aliyuncs.com/2020-07/-10/2c661c47-c739-4908-8c4f-53750493aea9_300516998945c70100aebc59ad42dcbf.jpg");
            try{
                userDao.insert(userEntity);
            }catch (Exception e)
            {
                e.printStackTrace();
                BaseResponse baseResponse = new BaseResponse(StatusCode.Fail);
                baseResponse.setData(e.getMessage());
                return baseResponse;
            }

            return new BaseResponse(StatusCode.Success);
        }

        //模糊搜索用户
       @RequestMapping("/searchByName")
       public BaseResponse  searchByName(String searchName,String name)
       {
           BaseResponse baseResponse = new BaseResponse(StatusCode.Success);
           List<UserEntity> list = userDao.selectList(new QueryWrapper<UserEntity>().like("name", searchName));
           List<UserEntity>  friendList= redisService.redisFindFriendList(name);
           HashMap<String,Integer> hashMap=new HashMap<>();
           friendList.stream().forEach(res->{
               hashMap.put(res.getName(),1);
           });
           hashMap.put(name,1);
           List<UserEntity> collect = list.stream().filter(res -> !hashMap.containsKey(res.getName())).collect(Collectors.toList());
           if(collect!=null)
           {
               baseResponse.setData(collect);

               return baseResponse;
           }
           else
           {
               baseResponse.setMsg("无数据");
               return  baseResponse;
           }
       }






}
