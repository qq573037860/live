package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.constant.LiveConfiguration;
import com.sjq.live.endpoint.netty.NettyEndPointSwitch;
import com.sjq.live.utils.NettyEventLoopFactory;
import io.netty.bootstrap.ServerBootstrap;
import io.netty.buffer.PooledByteBufAllocator;
import io.netty.channel.*;
import io.netty.channel.socket.SocketChannel;
import io.netty.handler.codec.http.HttpServerCodec;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnBean(NettyEndPointSwitch.class)
public class NettyServer implements InitializingBean {

    private static final Logger logger = LoggerFactory.getLogger(NettyServer.class);

    @Autowired
    private LiveConfiguration liveConfiguration;

    @Override
    public void afterPropertiesSet() throws Exception {
        NettyEndPointRegister.register();
        openHttpServer();
    }

    private void openHttpServer() {
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

                        /*ChannelPipeline p = ch.pipeline();
                        EventExecutor e1 = new DefaultEventExecutor(16);
                        EventExecutor e2 = new DefaultEventExecutor(8);

                        p.addLast(new MyProtocolCodec());
                        p.addLast(e1, new MyDatabaseAccessingHandler());
                        p.addLast(e2, new MyHardDiskAccessingHandler());*/

                        ch.pipeline()
                            .addLast(new HttpServerCodec())
                            //.addLast("httpAggregator",new HttpObjectAggregator(1024*1024)) // http 消息聚合器(解析body中的数据)
                            .addLast()
                            .addLast(new NettyHttpHandler());
                    }
                });
        // bind
        ChannelFuture channelFuture = bootstrap.bind(/*liveConfiguration.getServerPort()*/9999);
        channelFuture.syncUninterruptibly();
        Channel channel = channelFuture.channel();
        channel.closeFuture().addListener(future -> logger.info("服务器端关闭"));
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
