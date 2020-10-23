package com.zut.lpf.nettys;

import com.alibaba.fastjson.JSON;
import com.zut.lpf.controller.UserController;
import com.zut.lpf.entity.MsgEntity;
import com.zut.lpf.util.GlobalLock;
import com.zut.lpf.util.MychatApplication;
import io.netty.channel.Channel;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.SimpleChannelInboundHandler;
import io.netty.channel.group.ChannelGroup;
import io.netty.channel.group.DefaultChannelGroup;
import io.netty.handler.codec.http.websocketx.TextWebSocketFrame;
import io.netty.util.concurrent.GlobalEventExecutor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.core.AmqpAdmin;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.redis.core.RedisTemplate;
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

     public  static  MyTextWebSocketHandler myTextWebSocketHandler;

     //remoteId  to    channel
    public static ConcurrentHashMap<String, Channel> channelMap = new ConcurrentHashMap<>();

    public static List<Channel> channelList=new ArrayList<>();
    //定义一个channel组，关了所有的channel

    //GlobalEventExecutro.INSTANCE 全局事件执行器，单例
    private static ChannelGroup channelGroup=new DefaultChannelGroup(GlobalEventExecutor.INSTANCE);
    SimpleDateFormat simpleDateFormat=new SimpleDateFormat("hh:mm");

    @PostConstruct
    public void init()
    {
        myTextWebSocketHandler=this;
    }

    @Override
    public void handlerRemoved(ChannelHandlerContext ctx) throws Exception {
        GlobalLock.flag.remove(ctx.channel().id().toString());
        System.out.println("remove被调用"+ctx.channel().id());
    }

    @Override
    public void handlerAdded(ChannelHandlerContext ctx) throws Exception {

        //netty占用信号量
        GlobalLock.nettyLock.acquire();
        Channel channel = ctx.channel();
        log.info("{}加入聊天",channel.id().toString());
        channelGroup.add(channel);
        channelMap.put(channel.id().toString(),channel);
        GlobalLock.remoteId=channel.id().toString();
        //释放http信号量
        GlobalLock.HttpLock.release();


    }

    //使用TextWebSocketFrame传输数据
    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, TextWebSocketFrame textWebSocketFrame) throws Exception {


       MsgEntity msgEntity= JSON.parseObject(textWebSocketFrame.text().toString(),MsgEntity.class);
        msgEntity.setTime(simpleDateFormat.format(new Date()));
        log.info("服务器收到消息{}", msgEntity);
       if(msgEntity.getAcceptId().equals("all"))
       {
          myTextWebSocketHandler.rabbitTemplate.convertAndSend("mycat-fanout","kkk",msgEntity);
       }
       else
       {
            myTextWebSocketHandler.rabbitTemplate.convertAndSend("mycat-topic",msgEntity.getAcceptId(),msgEntity);
           myTextWebSocketHandler.rabbitTemplate.convertAndSend("mycat-topic",msgEntity.getName(),msgEntity);
       }

    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        GlobalLock.flag.remove(ctx.channel().id().toString());
      cause.printStackTrace();
       ctx.close();
    }

/*    @Override
    protected void channelRead0(ChannelHandlerContext channelHandlerContext, Object o) throws Exception {
        System.out.println((MsgEntity)JSON.parse(o.toString().getBytes()));
        if(o instanceof MsgEntity)
        {
            log.info("服务器收到消息{}",(MsgEntity)o);
        }

        channelGroup.forEach(res->{
            System.out.println(res.remoteAddress());
            res.writeAndFlush(new TextWebSocketFrame(simpleDateFormat.format(new Date())+"    "+res.remoteAddress()+"发送了信息: "+(MsgEntity)o));
        });
    }*/
}
