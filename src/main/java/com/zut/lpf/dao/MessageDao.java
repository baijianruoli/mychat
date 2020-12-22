package com.zut.lpf.dao;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.zut.lpf.entity.MessageEntity;
import com.zut.lpf.entity.MsgEntity;
import org.apache.ibatis.annotations.Mapper;
import org.springframework.stereotype.Component;

@Mapper
@Component
public interface MessageDao extends BaseMapper<MessageEntity> {
}
