package com.sjq.live.endpoint.netty.bootstrap;

import com.sjq.live.support.netty.NettyChannelAttribute;
import com.sjq.live.utils.IpAddressUtils;
import io.netty.channel.ChannelHandlerContext;
import io.netty.channel.ChannelInboundHandlerAdapter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public abstract class AbstractNettyHandler extends ChannelInboundHandlerAdapter {

    private static final Logger logger = LoggerFactory.getLogger(AbstractNettyHandler.class);

    protected abstract void doChannelInactive(ChannelHandlerContext ctx);

    protected abstract void doExceptionCaught(ChannelHandlerContext ctx, Throwable cause);

    @Override
    public void channelActive(ChannelHandlerContext ctx) throws Exception {
        String hostAddress = IpAddressUtils.getSocketIpPortInfo(ctx);
        NettyChannelAttribute.setHostAddress(ctx, hostAddress);
        logger.info("客户端[{}]成功连接服务器", hostAddress);
    }

    @Override
    public void channelInactive(ChannelHandlerContext ctx) throws Exception {
        //触发异常信息
        final String hostAddress = NettyChannelAttribute.getHostAddress(ctx);
        logger.info("客户端[{}]与服务器断开连接", hostAddress);
        try {
            doChannelInactive(ctx);
        } finally {
            NettyChannelAttribute.clearAllAttribute(ctx);
            ctx.fireChannelInactive();
            ctx.close();
        }
    }

    @Override
    public void exceptionCaught(ChannelHandlerContext ctx, Throwable cause) throws Exception {
        //触发异常信息
        final String hostAddress = NettyChannelAttribute.getHostAddress(ctx);
        logger.error("客户端[{}]与服务器连接异常", hostAddress, cause);
        try {
            doExceptionCaught(ctx, cause);
        } finally {
            NettyChannelAttribute.clearAllAttribute(ctx);
            ctx.fireExceptionCaught(cause);
            ctx.close();
        }
    }

}
