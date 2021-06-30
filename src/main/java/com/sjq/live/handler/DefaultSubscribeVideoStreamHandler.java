package com.sjq.live.handler;

import com.sjq.live.model.OperateResponse;
import com.sjq.live.model.RequestParam;
import com.sjq.live.support.AbstractStreamDistributeHandler;
import com.sjq.live.support.SubscribeHandler;
import org.apache.commons.lang3.StringUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service("defaultSubscribeVideoStreamHandler")
public class DefaultSubscribeVideoStreamHandler implements VideoStreamHandler {

    @Autowired
    private TransformStreamHandler transformStreamHandler;

    @Override
    public void afterConnectionEstablished(final RequestParam attribute) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void afterConnectionEstablished(final RequestParam attribute,
                                           final AbstractStreamDistributeHandler handler) {
        subscribe(attribute, handler);
    }

    @Override
    public void handleBinaryMessage(final RequestParam attribute,
                                    final byte[] data) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void afterConnectionClosed(final RequestParam attribute) {
        unSubscribe(attribute);
    }

    private void subscribe(final RequestParam attribute,
                           final AbstractStreamDistributeHandler handler) {
        if (StringUtils.isEmpty(attribute.getRegisterId())) {
            final OperateResponse<SubscribeHandler> operateResponse = transformStreamHandler.subscribe(attribute.getUserId(), attribute.getSubscribeId(), handler);
            if (operateResponse.isSuccess()) {
                attribute.setSubscribeHandler(operateResponse.getData());
            }
        }
    }

    private void unSubscribe(final RequestParam attribute) {
        final SubscribeHandler subscribeHandler = attribute.getSubscribeHandler();
        if (Objects.nonNull(subscribeHandler)) {
            subscribeHandler.unSubscribe();
        }
    }
}
