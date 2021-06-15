package com.sjq.live.endpoint.original;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Component;

@Component
@ConditionalOnProperty(value = "stream.transport", havingValue = "original")
public class OriginalEndPointSwitch {
}
