package com.zut.lpf.service.impl;

import org.springframework.stereotype.Service;

import java.util.Map;

import com.baomidou.mybatisplus.core.conditions.query.QueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.zut.common.utils.PageUtils;
import com.zut.common.utils.Query;

import com.zut.lpf.dao.MessageDao;
import com.zut.lpf.entity.MessageEntity;
import com.zut.lpf.service.MessageService;


@Service("messageService")
public class MessageServiceImpl extends ServiceImpl<MessageDao, MessageEntity> implements MessageService {

    @Override
    public PageUtils queryPage(Map<String, Object> params) {
        String key = (String) params.get("key");
        IPage<MessageEntity> page = this.page(
                new Query<MessageEntity>().getPage(params),
                new QueryWrapper<MessageEntity>().like("user_name", key).orderByDesc("create_time")
        );
        return new PageUtils(page);
    }

}