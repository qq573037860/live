package com.sjq.live.handler;

import com.sjq.live.model.RequestParam;
import com.sjq.live.support.AbstractStreamDistributeHandler;

public interface VideoStreamHandler {

    void afterConnectionEstablished(final RequestParam attribute);

    void afterConnectionEstablished(final RequestParam attribute, final AbstractStreamDistributeHandler handler);

    void handleBinaryMessage(final RequestParam attribute,
                             final byte[] data);

    void afterConnectionClosed(final RequestParam attribute);

}
