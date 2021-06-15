package com.sjq.live.endpoint.original.websocket.bootstrap;

import com.sjq.live.endpoint.original.OriginalEndPointSwitch;
import com.sjq.live.model.RequestParam;
import com.sjq.live.utils.HttpUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.http.server.ServerHttpRequest;
import org.springframework.http.server.ServerHttpResponse;
import org.springframework.stereotype.Component;
import org.springframework.web.socket.WebSocketHandler;
import org.springframework.web.socket.server.HandshakeInterceptor;

import java.util.Map;

/**
 * Created by shenjq on 2019/12/2
 */
@Component
@ConditionalOnBean(OriginalEndPointSwitch.class)
public class WebSocketInterceptor implements HandshakeInterceptor {

    private static final Logger logger = LoggerFactory.getLogger(WebSocketInterceptor.class);

    @Override
    public boolean beforeHandshake(ServerHttpRequest request, ServerHttpResponse response, WebSocketHandler webSocketHandler, Map<String, Object> map) throws Exception {
        logger.info("beforeHandshake, uri:{}", request.getURI().toString());

        // 获得请求参数
        final RequestParam urlParam = new RequestParam(HttpUtils.decodeParamMap(request.getURI().getQuery()));
        final RequestParam sessionAttributeMap = new RequestParam(map);

        String userId = urlParam.getUserId();
        if (StringUtils.isEmpty(userId)) {
            return false;
        }
        sessionAttributeMap.setUserId(userId);

        boolean isPass = false;
        String publishId = urlParam.getPublishId();
        if (!StringUtils.isEmpty(publishId)) {
            sessionAttributeMap.setPublishId(publishId);
            isPass = true;
        }
        String subscribeId = urlParam.getSubscribeId();
        if (!StringUtils.isEmpty(subscribeId)) {
            sessionAttributeMap.setSubscribeId(subscribeId);
            isPass = true;
        }
        return isPass;
    }

    @Override
    public void afterHandshake(ServerHttpRequest serverHttpRequest, ServerHttpResponse serverHttpResponse, WebSocketHandler webSocketHandler, Exception e) {
    }
}
