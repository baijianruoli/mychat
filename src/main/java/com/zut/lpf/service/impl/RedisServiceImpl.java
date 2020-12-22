package com.zut.lpf.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.zut.lpf.dao.UserDao;
import com.zut.lpf.entity.UserEntity;
import com.zut.lpf.service.MsgService;
import com.zut.lpf.service.RedisService;
import com.zut.lpf.service.UserService;
import com.zut.lpf.util.GlobalCode;
import com.zut.lpf.util.GlobalLock;
import com.zut.lpf.vo.UvVo;
import org.springframework.amqp.core.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Service;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Service
public class RedisServiceImpl implements RedisService {

    @Autowired
    private RedisTemplate redisTemplate;
    @Autowired
    private UserDao userDao;
    @Autowired
    private AmqpAdmin amqpAdmin;
    @Autowired
    private MsgService msgService;

    private static SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy/MM/dd");

    /**
     * @param name
     * @return 查找好友list
     */
    public List<UserEntity> redisFindFriendList(String name) {
        List<UserEntity> list = new ArrayList<>();
        while (redisTemplate.boundListOps(name).size() > 0) {
            Object o = redisTemplate.boundListOps(name).leftPop();
            UserEntity userList = (UserEntity) o;
            list.add(userList);
        }
        list.stream().forEach(res -> {
            redisTemplate.boundListOps(name).rightPush(res);
        });

        return list;
    }

    //添加朋友
    @Override
    public void redisAddFriend(String friendName, String name) {
        UserEntity userEntity = userDao.selectOne(new QueryWrapper<UserEntity>().eq("name", friendName));
        userEntity.setFriendFlag(1);
        redisTemplate.boundListOps(name).rightPush(userEntity);

    }

    //删除朋友
    @Override
    public void redisRemoveFriend(String friendName, String name) {
        //删除消息
        msgService.deleteFriendMsg(name, friendName);
        List<UserEntity> list = new ArrayList<>();
        while (redisTemplate.boundListOps(name).size() > 0) {
            Object o = redisTemplate.boundListOps(name).leftPop();
            UserEntity userList = (UserEntity) o;
            list.add(userList);
        }
        List<UserEntity> collect = list.stream().filter(res ->
                !res.getName().equals(friendName)
        ).collect(Collectors.toList());
        collect.stream().forEach(res -> {
            redisTemplate.boundListOps(name).rightPush(res);
        });
    }

    //判断name是否是friendName的好友
    @Override
    public boolean hashFrined(String friendName, String name) {

        List<UserEntity> list = redisFindFriendList(friendName);
        int flag = GlobalCode.CODE_FAIL;
        for (UserEntity u : list) {
            if (u.getName().equals(name))
                flag = GlobalCode.CODE_OK;
        }
        if (flag == GlobalCode.CODE_OK)
            return true;
        else
            return false;
    }

    //判断消息队列是否创建
    public void createQueue(String name) {
        if (!redisTemplate.boundHashOps("mycat").hasKey(name)) {
            redisTemplate.boundHashOps("mycat").put(name, GlobalCode.CODE_OK);
            amqpAdmin.declareQueue(new Queue(name));
            amqpAdmin.declareExchange(new TopicExchange(GlobalCode.TOPIC_EXCHANGE));
            amqpAdmin.declareExchange(new FanoutExchange(GlobalCode.FANOUT_EXCHANGE));
            amqpAdmin.declareBinding(new Binding(name, Binding.DestinationType.QUEUE, GlobalCode.FANOUT_EXCHANGE, "kkk", null));
            amqpAdmin.declareBinding(new Binding(name, Binding.DestinationType.QUEUE, GlobalCode.TOPIC_EXCHANGE, name, null));
        }
    }

    //加入Redis的uv统计
    @Async
    @Override
    public void UvLoad(int id) {
        HyperLogLogOperations hyperLogLogOperations = redisTemplate.opsForHyperLogLog();
        Date date = new Date();
        hyperLogLogOperations.add(simpleDateFormat.format(date), id);
    }

    public List<UvVo> getHyperLog() {
        HyperLogLogOperations hyperLogLogOperations = redisTemplate.opsForHyperLogLog();
        ArrayList<UvVo> list = new ArrayList<>();
        Date date = new Date();
        Long size = hyperLogLogOperations.size(simpleDateFormat.format(date));
        HashOperations hashOperations = redisTemplate.opsForHash();
        Map<String, Long> entries = hashOperations.entries(GlobalCode.HYPER_LOG_LOG);
        for (String i : entries.keySet()) {
            list.add(new UvVo(i, entries.get(i)));
        }
        list.add(new UvVo(simpleDateFormat.format(date), size));
        return list;
    }

    //每天0点插入Uv数据
    public void insertHyperLogLog() {
        HashOperations hashOperations = redisTemplate.opsForHash();
        HyperLogLogOperations hyperLogLogOperations = redisTemplate.opsForHyperLogLog();
        Long size = hyperLogLogOperations.size(simpleDateFormat.format(new Date()));
        hashOperations.put(GlobalCode.HYPER_LOG_LOG, simpleDateFormat.format(new Date()), size);
    }
}
