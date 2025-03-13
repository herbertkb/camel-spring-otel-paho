package com.example.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class BodyLengthProcessor implements Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(BodyLengthProcessor.class);

    private Integer maxBodyLength;

    public BodyLengthProcessor(Integer maxBodyLength) {
        this.maxBodyLength = maxBodyLength;
    }

    public void process(Exchange exchange) throws Exception {
        String body = exchange.getIn().getBody(String.class);
        int bodyLength = body.length()  < maxBodyLength ? body.length() : maxBodyLength;
        exchange.getIn().setBody(body.substring(0, bodyLength));        
    }

}
