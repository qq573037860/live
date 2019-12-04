package com.sjq.live.service;

import com.sjq.live.utils.HttpUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Created by shenjq on 2019/12/2
 */
@Component
public class WebSocketInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        // 获得请求参数
        Map<String, String> paramMap = HttpUtils.decodeParamMap(request.getURI().getQuery());
        boolean isPass = false;
        String publishId = paramMap.get("publishId");
        if (!StringUtils.isEmpty(publishId)) {
            map.put("publishId", publishId);
            isPass = true;
        }
        String subscribeId = paramMap.get("subscribeId");
        if (!StringUtils.isEmpty(subscribeId)) {
            map.put("subscribeId", subscribeId);
            isPass = true;
        }
        return isPass;
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {

    }
}
