package com.sjq.live.service;

import com.sjq.live.model.WebSocketAttribute;
import com.sjq.live.support.AbstractStreamDistributeHandler;

public interface VideoStreamHandler {

    void afterConnectionEstablished(final WebSocketAttribute attribute);

    void afterConnectionEstablished(final WebSocketAttribute attribute, final AbstractStreamDistributeHandler handler);

    void handleBinaryMessage(final WebSocketAttribute attribute,
                             final byte[] data);

    void afterConnectionClosed(final WebSocketAttribute attribute);

}
