package com.zut.lpf.service.impl;

import com.alibaba.fastjson.JSON;
import com.zut.lpf.entity.MsgEntity;
import com.zut.lpf.nettys.MyTextWebSocketHandler;
import com.zut.lpf.service.ExecutorChannel;
import com.zut.lpf.util.GlobalLock;
import io.netty.channel.Channel;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
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
    @Override
    public void executorSumbit(String name) {
//        try {
//            Thread.sleep(2000);
//        } catch (InterruptedException e) {
//            e.printStackTrace();
//        }
        executorService.submit(()->{

            while(true)
            {
                if(!GlobalLock.flag.containsKey(GlobalLock.humanToChannelId.get(name)))
                    break;
                String remoteId = GlobalLock.humanToChannelId.get(name);
                Object msg = rabbitTemplate.receiveAndConvert(name);
                if(msg!=null)
                {
                    Channel channel = MyTextWebSocketHandler.channelMap.get(remoteId);
                    System.out.println(channel.remoteAddress());
                    System.out.println(msg);
                    channel.writeAndFlush(new TextWebSocketFrame(JSON.toJSONString((MsgEntity)msg)));
                }
//                              Thread.sleep(2000);
            }

        });
    }
}
