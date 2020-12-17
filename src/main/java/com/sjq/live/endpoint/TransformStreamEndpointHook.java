package com.sjq.live.endpoint;

import com.sjq.live.support.InputStreamProcessor;
import com.sjq.live.support.OutputStreamProcessor;

public interface TransformStreamEndpointHook {

    void originStreamReach(final String publishId,
                           final OutputStreamProcessor streamProcessor);

    void transformedStreamReach(final String publishId,
                                final InputStreamProcessor streamProcessor);

}
