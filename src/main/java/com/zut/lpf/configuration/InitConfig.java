package com.zut.lpf.configuration;

import com.zut.lpf.entity.UserEntity;
import com.zut.lpf.nettys.MyTextWebSocketHandler;
import com.zut.lpf.util.GlobalCode;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelInitializer;
import io.netty.channel.ChannelPipeline;
import io.netty.channel.nio.NioEventLoopGroup;
import io.netty.channel.socket.SocketChannel;
import io.netty.channel.socket.nio.NioServerSocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import io.netty.handler.codec.http.websocketx.WebSocketServerProtocolHandler;
import io.netty.handler.logging.LogLevel;
import io.netty.handler.logging.LoggingHandler;
import io.netty.handler.stream.ChunkedWriteHandler;
import lombok.extern.slf4j.Slf4j;
import org.springframework.amqp.rabbit.core.RabbitTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.data.redis.core.HashOperations;
import org.springframework.data.redis.core.HyperLogLogOperations;
import org.springframework.data.redis.core.RedisTemplate;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

@Component
@Slf4j
public class InitConfig implements CommandLineRunner {

    @Autowired
    RabbitTemplate rabbitTemplate;

    @Autowired
    RedisTemplate redisTemplate;

    @Override
    public void run(String... args) throws Exception {

//        HashOperations hashOperations = redisTemplate.opsForHash();
//        hashOperations.put(GlobalCode.HYPER_LOG_LOG, "2020/11/20", 15l);
//        hashOperations.put(GlobalCode.HYPER_LOG_LOG, "2020/11/21", 22l);
//        hashOperations.put(GlobalCode.HYPER_LOG_LOG, "2020/11/22", 7l);
//        hashOperations.put(GlobalCode.HYPER_LOG_LOG, "2020/11/23", 30l);
        NioEventLoopGroup bossGroup = new NioEventLoopGroup(1);
        NioEventLoopGroup workerGroup = new NioEventLoopGroup();
        try {
            ServerBootstrap serverBootstrap = new ServerBootstrap();
            serverBootstrap.group(bossGroup, workerGroup)
                    .channel(NioServerSocketChannel.class)
                    .handler(new LoggingHandler(LogLevel.DEBUG))
                    .childHandler(new ChannelInitializer<SocketChannel>() {
                        @Override
                        protected void initChannel(SocketChannel socketChannel) throws Exception {
                            ChannelPipeline pipeline = socketChannel.pipeline();
                            pipeline.addLast(new HttpServerCodec());
                            // 以块方式写
                            pipeline.addLast(new ChunkedWriteHandler());
                            /**
                             * http数据是分段的，HttpObjectAggregator将多个段聚合起来
                             */
                            pipeline.addLast(new HttpObjectAggregator(8192));
                            // WebSocketServerProtocolHandler核心功能是将http协议升级为ws协议
                            pipeline.addLast(new WebSocketServerProtocolHandler("/hello"));
                            pipeline.addLast(new MyTextWebSocketHandler());
                        }
                    });
            log.info("netty启动成功");
            ChannelFuture sync = serverBootstrap.bind(8002).sync();
            sync.channel().closeFuture().sync();
        } catch (InterruptedException e) {
            e.printStackTrace();
        } finally {
            bossGroup.shutdownGracefully();
            workerGroup.shutdownGracefully();
        }

    }

}
