package com.sjq.live.service;

import com.sjq.live.model.WebSocketAttribute;

public interface PublishVideoStreamProcessor {

    void afterConnectionEstablished(final WebSocketAttribute attribute);

    void handleBinaryMessage(final WebSocketAttribute attribute,
                             final byte[] data);

    void afterConnectionClosed(final WebSocketAttribute attribute);

}
