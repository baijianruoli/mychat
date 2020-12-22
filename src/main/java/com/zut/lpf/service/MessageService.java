package com.zut.lpf.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.zut.common.utils.PageUtils;
import com.zut.lpf.entity.MessageEntity;

import java.util.Map;

/**
 * @author liqiqi
 * @email 523892377@qq.com
 * @date 2020-11-23 11:03:48
 */
public interface MessageService extends IService<MessageEntity> {

    PageUtils queryPage(Map<String, Object> params);
}

