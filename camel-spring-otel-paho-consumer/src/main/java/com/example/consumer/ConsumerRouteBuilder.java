package com.example.consumer;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.opentelemetry.OpenTelemetryTracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import com.example.consumer.otel.EndSpanProcessor;
import com.example.consumer.otel.StartSpanProcessor;

import static org.apache.camel.LoggingLevel.INFO;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import org.apache.camel.CamelContext;

@Component
public class ConsumerRouteBuilder extends RouteBuilder {
    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerRouteBuilder.class);

    @Autowired
    CamelContext camelContext;

    @Value("${app.max-body-length}")
    private Integer maxBodyLength;


    @Override
    public void configure() throws Exception {

        OpenTelemetryTracer otelTracer = new OpenTelemetryTracer();
        otelTracer.init(camelContext);

        from("paho-mqtt5:{{app.topic}}")
            .id("route-paho-consumer")
            .process(new StartSpanProcessor(otelTracer))
            .to("direct:sub");

        from("direct:sub")
            .id("route-paho-consumer-process")
            .delay(simple("{{app.delay}}"))
            .log(INFO, "HEADERS: ${in.headers}")
            .process(new BodyLengthProcessor(maxBodyLength))
            .log(INFO, "BODY: ${body}")
            .process(new EndSpanProcessor())
            ;
    }
}
