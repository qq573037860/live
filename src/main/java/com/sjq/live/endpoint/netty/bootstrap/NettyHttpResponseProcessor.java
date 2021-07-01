package com.sjq.live.endpoint.netty.bootstrap;

import com.alibaba.fastjson.JSONObject;
import com.sjq.live.model.NettyHttpRequest;
import com.sjq.live.support.netty.NettyChannelAttribute;
import com.sjq.live.utils.NettyUtils;
import io.netty.channel.ChannelFuture;
import io.netty.channel.ChannelHandlerContext;
import org.apache.commons.lang3.StringUtils;
import java.lang.reflect.Method;

/**
 * HTTP 返回处理器
 */
public class NettyHttpResponseProcessor {

    /**
     * 处理http接口方法返回值
     * @param method
     * @param nettyHttpRequest
     * @param response
     * @param context
     */
    public static void processResponse(Method method,
                                       NettyHttpRequest nettyHttpRequest,
                                       Object response,
                                       ChannelHandlerContext context) {

        if (method.getReturnType() == Void.TYPE) {
            return;
        }

        final ChannelFuture future;
        if (method.getReturnType() == String.class) {
            String result = response.toString();
            if (isStaticResource(result)) {
                //处理静态文件资源
                future = processStaticResourceResponse(context, nettyHttpRequest,result);
            } else {
                //直接返回String
                future = response(context, result);
            }
        } else {
            if (method.isAnnotationPresent(NettyResponseBody.class)) {
                // 返回json
                future = response(context, JSONObject.toJSONString(response));
            } else {
                future = response(context, response.toString());
            }
        }

        context.flush();
        NettyChannelAttribute.setLastChannelFuture(context, future);
    }

    /**
     * 返回字符类型结果
     * @param context
     * @param result
     */
    private static ChannelFuture response(ChannelHandlerContext context, String result) {
        return NettyUtils.writeHttpOkResponse(context, result.getBytes());
    }

    /**
     * 处理静态资源请求返回值
     * @param context
     * @param nettyHttpRequest
     * @param path
     */
    private static ChannelFuture processStaticResourceResponse(ChannelHandlerContext context, NettyHttpRequest nettyHttpRequest, String path) {
        return NettyUtils.writeHttpOkResponse(context, nettyHttpRequest.getHttpVersion(), nettyHttpRequest.isKeepAlive(), path);
    }


    private static boolean isStaticResource(String result) {
        return StringUtils.startsWithAny(result, "static", "templates");
    }

}
