package com.sjq.live.endpoint.original;

import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Controller;

@Controller
/*@ConditionalOnProperty(value = "stream.transport", havingValue = "original")*/
public class OriginalEndPointBootstrap {

    @Bean
    public OriginalTransformStreamEndpoint initOriginalTransformStreamEndpoint(){
        return new OriginalTransformStreamEndpoint();
    }

}
