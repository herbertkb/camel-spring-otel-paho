package com.example.consumer.otel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.trace.Span;

public class EndSpanProcessor implements Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(EndSpanProcessor.class);

    public void process(Exchange exchange) throws Exception {
        Span span = exchange.getProperty("span", Span.class);
        if (span != null) {
            span.end();
        }
    }

}
