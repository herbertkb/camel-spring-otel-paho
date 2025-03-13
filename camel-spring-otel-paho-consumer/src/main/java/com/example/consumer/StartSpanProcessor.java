package com.example.consumer;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.opentelemetry.OpenTelemetryTracer;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;

import static org.apache.camel.component.paho.mqtt5.PahoMqtt5Constants.CAMEL_PAHO_MSG_PROPERTIES;

public class StartSpanProcessor implements Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartSpanProcessor.class);

    private OpenTelemetryTracer otelTracer;

    public StartSpanProcessor(OpenTelemetryTracer otelTracer) {
        this.otelTracer = otelTracer;
    }

    public void process(Exchange exchange) throws Exception {
         String traceParent = exchange.getMessage().getHeader(CAMEL_PAHO_MSG_PROPERTIES, MqttProperties.class)
                    .getUserProperties().stream()
                    .filter(up -> up.getKey().equals("traceparent"))
                    .findFirst().get().getValue();                
                String traceId = traceParent.substring(3,35);
                String spanId = traceParent.substring(36,52);

                LOGGER.debug("TRACEPARENT: "+traceParent);
                LOGGER.debug("traceId: "+traceId);
                LOGGER.debug("spanId: "+spanId);

                SpanContext remoteContext = SpanContext.createFromRemoteParent(
                    traceId,
                    spanId,
                    TraceFlags.getSampled(),
                    TraceState.getDefault());

                SpanBuilder sb = otelTracer.getTracer().spanBuilder("paho-consumer");
                sb.setParent(Context.current().with(Span.wrap(remoteContext)));
                Span span = sb.startSpan();

                exchange.setProperty("span", span);
    }

}
