package com.sjq.live.service;

import com.sjq.live.model.OperateResponse;
import com.sjq.live.model.WebSocketAttribute;
import com.sjq.live.support.AbstractStreamDistributeHandler;
import com.sjq.live.support.PublishHandler;
import com.sjq.live.support.SubscribeHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.web.socket.WebSocketSession;

import java.util.Objects;

@Service("defaultSubscribeVideoStreamHandler")
public class DefaultSubscribeVideoStreamHandler implements VideoStreamHandler {

    @Autowired
    private TransformStreamHandler transformStreamHandler;

    @Override
    public void afterConnectionEstablished(final WebSocketAttribute attribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void afterConnectionEstablished(final WebSocketAttribute attribute,
                                           final AbstractStreamDistributeHandler handler) {
        subscribe(attribute, handler);
    }

    @Override
    public void handleBinaryMessage(final WebSocketAttribute attribute,
                                    final byte[] data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void afterConnectionClosed(final WebSocketAttribute attribute) {
        unSubscribe(attribute);
    }

    private void subscribe(final WebSocketAttribute attribute,
                           final AbstractStreamDistributeHandler handler) {
        if (StringUtils.isEmpty(attribute.getRegisterId())) {
            final OperateResponse<SubscribeHandler> operateResponse = transformStreamHandler.subscribe(attribute.getUserId(), attribute.getSubscribeId(), handler);
            if (operateResponse.isSuccess()) {
                attribute.setSubscribeHandler(operateResponse.getData());
            }
        }
    }

    private void unSubscribe(final WebSocketAttribute attribute) {
        final SubscribeHandler subscribeHandler = attribute.getSubscribeHandler();
        if (Objects.nonNull(subscribeHandler)) {
            subscribeHandler.unSubscribe();
        }
    }
}
