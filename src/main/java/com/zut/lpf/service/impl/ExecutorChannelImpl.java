package com.zut.lpf.service.impl;

import com.alibaba.fastjson.JSON;
import com.zut.lpf.entity.MsgEntity;
import com.zut.lpf.nettys.MyTextWebSocketHandler;
import com.zut.lpf.service.ExecutorChannel;
import com.zut.lpf.util.GlobalLock;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

@Service
public class ExecutorChannelImpl implements ExecutorChannel {

    public static ExecutorService executorService = Executors.newFixedThreadPool(100000);
    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private AmqpAdmin amqpAdmin;

    @Override
    public void executorSumbit(String name) {
        amqpAdmin.purgeQueue(name);
        executorService.submit(() -> {
            while (true) {
                if (!GlobalLock.flag.containsKey(GlobalLock.humanToChannelId.get(name)))
                    break;
                String remoteId = GlobalLock.humanToChannelId.get(name);
                Object msg = rabbitTemplate.receiveAndConvert(name);
                if (msg != null) {
                    Channel channel = MyTextWebSocketHandler.channelMap.get(remoteId);
                    channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString((MsgEntity) msg)));
                }
            }

        });
    }
}
