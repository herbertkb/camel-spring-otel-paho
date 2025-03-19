package com.example.consumer.otel;

import org.apache.camel.Exchange;
import org.apache.camel.Processor;
import org.apache.camel.opentelemetry.OpenTelemetryTracer;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import io.opentelemetry.api.trace.Span;
import io.opentelemetry.api.trace.SpanBuilder;
import io.opentelemetry.api.trace.SpanContext;
import io.opentelemetry.api.trace.TraceFlags;
import io.opentelemetry.api.trace.TraceState;
import io.opentelemetry.context.Context;

import static org.apache.camel.component.paho.mqtt5.PahoMqtt5Constants.CAMEL_PAHO_MSG_PROPERTIES;

import java.util.Optional;

public class StartSpanProcessor implements Processor {
    private static final Logger LOGGER = LoggerFactory.getLogger(StartSpanProcessor.class);

    private OpenTelemetryTracer otelTracer;

    public StartSpanProcessor(OpenTelemetryTracer otelTracer) {
        this.otelTracer = otelTracer;
    }

    public void process(Exchange exchange) throws Exception {
        Optional<UserProperty> traceParentUserProperty = exchange.getMessage().getHeader(CAMEL_PAHO_MSG_PROPERTIES, MqttProperties.class)
                .getUserProperties().stream()
                .filter(up -> up.getKey().equals("traceparent"))
                .findFirst();

        if (traceParentUserProperty.isEmpty()) {
            LOGGER.warn("No traceparent for message {}", exchange.getMessage().getMessageId());
            return;
        }

        String traceParent = traceParentUserProperty.get().getValue();
        String traceId = traceParent.substring(3, 35);
        String spanId = traceParent.substring(36, 52);

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
