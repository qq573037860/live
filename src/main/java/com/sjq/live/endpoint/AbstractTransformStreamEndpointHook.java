package com.sjq.live.endpoint;

import com.sjq.live.service.TransformStream;
import com.sjq.live.support.InputStreamProcessor;
import com.sjq.live.support.OutputStreamProcessor;
import org.springframework.beans.factory.annotation.Autowired;


public abstract class AbstractTransformStreamEndpointHook implements TransformStreamEndpointHook {

    @Autowired
    private TransformStream transformStream;

    @Override
    public void originStreamReach(final String publishId,
                                  final OutputStreamProcessor streamProcessor) {
        transformStream.processOriginalStream(publishId, streamProcessor);
    }

    @Override
    public void transformedStreamReach(final String publishId,
                                       final InputStreamProcessor streamProcessor) {
        transformStream.processTransformedStream(publishId, streamProcessor);
    }
}
