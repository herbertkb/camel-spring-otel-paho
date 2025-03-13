package com.example.consumer;

import org.apache.camel.builder.RouteBuilder;
import org.apache.camel.opentelemetry.OpenTelemetryTracer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.api.trace.Tracer;
import io.opentelemetry.context.Context;

import static org.apache.camel.LoggingLevel.INFO;

import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static org.apache.camel.component.paho.mqtt5.PahoMqtt5Constants.CAMEL_PAHO_MSG_PROPERTIES;

import java.io.IOException;

import org.apache.camel.CamelContext;

import io.opentelemetry.context.propagation.TextMapGetter;
import jakarta.annotation.PreDestroy;



@Component
public class ConsumerRouteBuilder extends RouteBuilder {

    @Autowired
    CamelContext camelContext;

    // @Autowired
    // OpenTelemetryTracer otelTracer;

    // @PreDestroy
    // public void destroy() throws IOException {
    //     otelTracer.close();
    // }

    private static final Logger LOGGER = LoggerFactory.getLogger(ConsumerRouteBuilder.class);

    @Override
    public void configure() throws Exception {

        OpenTelemetryTracer otelTracer = new OpenTelemetryTracer();
        otelTracer.init(camelContext);


        from("paho-mqtt5:{{app.topic}}")
            .id("route-paho-consumer")
            .process(exchange -> {
                String traceParent = exchange.getMessage().getHeader(CAMEL_PAHO_MSG_PROPERTIES, MqttProperties.class)
                    .getUserProperties().stream()
                    .filter(up -> up.getKey().equals("traceparent"))
                    .findFirst().get().getValue();                
                String traceId = traceParent.substring(3,35);
                String spanId = traceParent.substring(36,52);

                LOGGER.info("TRACEPARENT: "+traceParent);
                LOGGER.info("traceId: "+traceId);
                LOGGER.info("spanId: "+spanId);

                SpanContext remoteContext = SpanContext.createFromRemoteParent(
                    traceId,
                    spanId,
                    TraceFlags.getSampled(),
                    TraceState.getDefault());

                SpanBuilder sb = otelTracer.getTracer().spanBuilder("paho-consumer");
                sb.setParent(Context.current().with(Span.wrap(remoteContext)));
                Span span = sb.startSpan();

                exchange.getIn().setHeader("span", span);
            })

            // .process(exchange -> {
            //     String traceParent = exchange.getMessage().getHeader(CAMEL_PAHO_MSG_PROPERTIES, MqttProperties.class)
            //         .getUserProperties().stream()
            //         .filter(up -> up.getKey().equals("traceparent"))
            //         .findFirst().get().getValue();
            //     //TODO: DELETE THIS
            //     System.out.println("TRACEPARENT FROM REMOTE: "+traceParent);
            //     exchange.getIn().setHeader("traceparent", traceParent);                
            // })
            .to("direct:sub");

        from("direct:sub")
            .id("route-paho-consumer-process")
            .delay(simple("{{app.delay}}"))
            .log("HEADERS: ${in.headers}")

            // .process(exchange -> {
            //     // Tracer tracer = exchange.getContext().getRegistry()
            //     //     .lookupByNameAndType("tracer", OpenTelemetryTracer.class ).getTracer();
            //     // tracer.spanBuilder("paho-consumer").setParent(Context.current().with());
            //     Tracer tracer = otelTracer.getTracer();

            //     // Span.current().setAttribute("traceparent", null);
            //     String traceParent = exchange.getMessage().getHeader(CAMEL_PAHO_MSG_PROPERTIES, MqttProperties.class)
            //         .getUserProperties().stream()
            //         .filter(up -> up.getKey().equals("traceparent"))
            //         .findFirst().get().getValue();

            //     //TODO: DELETE THIS
            //     LOGGER.info("TRACEPARENT: "+traceParent);

                
            //     SpanContext fromProducerContext = SpanContext.createFromRemoteParent(
            //         traceParent, "", TraceFlags.getSampled(), TraceState.getDefault());

            //     LOGGER.info("fromProducerContext: "+fromProducerContext);


            //     SpanBuilder sb = tracer.spanBuilder("paho-consumer");
            //     sb.setParent(Context.current().with(Span.wrap(fromProducerContext)));
            //     sb.startSpan();
            // })

            .log(INFO, "BODY: ${body}")
            .process(exchange -> {
                exchange.getIn().getHeader("span", Span.class).end();
            })
            ;
    }

    
}
