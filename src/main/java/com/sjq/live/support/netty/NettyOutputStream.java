package com.sjq.live.support.netty;


import com.sjq.live.utils.NettyUtils;
import io.netty.channel.ChannelHandlerContext;

public class NettyOutputStream {

    private ChannelHandlerContext ctx;

    public NettyOutputStream(ChannelHandlerContext ctx) {
        this.ctx = ctx;

        preprocess();
    }

    private void preprocess() {
        //开头先返回一个response
        NettyUtils.writeHttpChunkResponse(ctx);
    }

    public void write(byte[] bytes) {
        NettyUtils.writeChunkContentResponse(ctx, bytes);
    }

    public void flush() {
        ctx.flush();
    }

    public void close() {
        NettyUtils.writeLastEmptyContentResponse(ctx);
    }
}
