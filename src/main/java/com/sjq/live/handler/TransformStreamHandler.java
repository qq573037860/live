package com.sjq.live.handler;

import com.sjq.live.model.LiveException;
import com.sjq.live.model.OperateResponse;
import com.sjq.live.support.*;

public interface TransformStreamHandler {

    OperateResponse<PublishHandler> publish(String publishId) throws LiveException;

    OperateResponse<SubscribeHandler> subscribe(final String userId,
                                                final String subscribeId,
                                                final AbstractStreamDistributeHandler handler);

    void processOriginalStream(final String publishId,
                               final OutputStreamProcessor streamProcessor);

    void processTransformedStream(final String publishId,
                                  final InputStreamProcessor streamProcessor);
}
