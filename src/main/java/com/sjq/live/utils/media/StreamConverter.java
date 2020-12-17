package com.sjq.live.utils.media;

import com.sjq.live.model.LiveException;

public interface StreamConverter {

    StreamConverterHandler startConvert(final String originStreamUrl,
                                        final String transformedStreamUrl) throws LiveException;

}
