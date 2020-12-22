package com.zut.lpf.nettys;

import com.alibaba.fastjson.JSON;
import com.zut.lpf.entity.MessageEntity;
import com.zut.lpf.entity.MsgEntity;
import com.zut.lpf.service.MsgService;
import com.zut.lpf.service.RedisService;
import com.zut.lpf.util.GlobalCode;
import com.zut.lpf.util.GlobalLock;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

//TextWebSocketFrame表示一个文本帧
@Slf4j
@Component
public class MyTextWebSocketHandler extends SimpleChannelInboundHandler<TextWebSocketFrame> {

    @Autowired
    private RabbitTemplate rabbitTemplate;
    @Autowired
    private RedisService redisService;
    @Autowired
    private MsgService msgService;
    public static MyTextWebSocketHandler myTextWebSocketHandler;
    //remoteId  to    channel
    public static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();
    //    private static ChannelGroup channelGroup=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM-dd HH:mm");

    @PostConstruct
    public void init() {
        myTextWebSocketHandler = this;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        GlobalLock.flag.remove(ctx.channel().id().toString());
        System.out.println("remove被调用" + ctx.channel().id());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {
        //netty占用信号量
        GlobalLock.nettyLock.acquire();
        Channel channel = ctx.channel();
        log.info("{}加入聊天", channel.remoteAddress());
        channelMap.put(channel.id().toString(), channel);
        GlobalLock.remoteId = channel.id().toString();
        //释放http信号量
        GlobalLock.HttpLock.release();
    }

    //使用TextWebSocketFrame传输数据
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {
        MsgEntity msgEntity = JSON.parseObject(textWebSocketFrame.text().toString(), MsgEntity.class);
        System.out.println(msgEntity);
        MessageEntity messageEntity = new MessageEntity();
        BeanUtils.copyProperties(msgEntity, messageEntity);
        messageEntity.setTime(new Date());
        msgEntity.setTime(simpleDateFormat.format(new Date()));
        msgEntity.setFriendFlag(GlobalCode.CODE_OK);
        //

        if (msgEntity.getAcceptId().equals("all")) {
            myTextWebSocketHandler.msgService.loadMessage(messageEntity);
            myTextWebSocketHandler.rabbitTemplate.convertAndSend(GlobalCode.FANOUT_EXCHANGE, "kkk", msgEntity);

        } else {
            myTextWebSocketHandler.msgService.loadMessage(messageEntity);
            myTextWebSocketHandler.rabbitTemplate.convertAndSend(GlobalCode.TOPIC_EXCHANGE, msgEntity.getName(), msgEntity);
            if (myTextWebSocketHandler.redisService.hashFrined(msgEntity.getAcceptId(), msgEntity.getName())) {
                msgEntity.setFriendFlag(GlobalCode.CODE_OK);
            } else {
                msgEntity.setFriendFlag(GlobalCode.CODE_FAIL);
            }
            if (GlobalLock.humanToChannelId.containsKey(msgEntity.getAcceptId())&&GlobalLock.flag.containsKey(GlobalLock.humanToChannelId.get(msgEntity.getAcceptId())))
                myTextWebSocketHandler.rabbitTemplate.convertAndSend(GlobalCode.TOPIC_EXCHANGE, msgEntity.getAcceptId(), msgEntity);
           String kkk[]={"1","2"};


        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        GlobalLock.flag.remove(ctx.channel().id().toString());
        cause.printStackTrace();
        ctx.close();
    }


}
