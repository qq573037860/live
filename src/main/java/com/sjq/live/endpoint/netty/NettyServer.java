package com.sjq.live.endpoint.netty;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.TransformStreamEndpointHook;
import com.sjq.live.utils.NettyEventLoopFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpObjectAggregator;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;

@Component
@ConditionalOnProperty(value = "stream.transport", havingValue = "netty")
public class NettyServer {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private TransformStreamEndpointHook transformStreamEndpointHook;
    @Autowired
    private LiveConfiguration liveConfiguration;

    @PostConstruct
    private void init() {
        doOpen();
    }

    private void doOpen() {
        ServerBootstrap bootstrap = new ServerBootstrap();

        EventLoopGroup bossGroup = NettyEventLoopFactory.eventLoopGroup(1, "NettyServerBoss");
        EventLoopGroup workerGroup = NettyEventLoopFactory.eventLoopGroup(
                10,
                "NettyServerWorker");

        bootstrap.group(bossGroup, workerGroup)
                .channel(NettyEventLoopFactory.serverSocketChannelClass())
                .option(ChannelOption.SO_REUSEADDR, Boolean.TRUE)
                .childOption(ChannelOption.TCP_NODELAY, Boolean.TRUE)
                .childOption(ChannelOption.ALLOCATOR, PooledByteBufAllocator.DEFAULT)
                .childHandler(new ChannelInitializer<SocketChannel>() {
                    @Override
                    protected void initChannel(SocketChannel ch) {
                        ch.pipeline()
                            .addLast(new HttpServerCodec())
                            .addLast("httpAggregator",new HttpObjectAggregator(1024*1024)) // http 消息聚合器(解析body中的数据)
                            .addLast(new NettyHttpHandler(transformStreamEndpointHook));
                    }
                });
        // bind
        ChannelFuture channelFuture = bootstrap.bind(liveConfiguration.getServerPort());
        channelFuture.syncUninterruptibly();
        Channel channel = channelFuture.channel();
        channel.closeFuture().addListener(future -> {logger.info("服务器端关闭");});
        logger.info("netty server starts successfully");
    }

    /*protected void doClose() {
        try {
            if (channel != null) {
                // unbind.
                channel.close();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
        try {
            if (bootstrap != null) {
                bossGroup.shutdownGracefully().syncUninterruptibly();
                workerGroup.shutdownGracefully().syncUninterruptibly();
            }
        } catch (Throwable e) {
            logger.warn(e.getMessage(), e);
        }
    }*/
}
