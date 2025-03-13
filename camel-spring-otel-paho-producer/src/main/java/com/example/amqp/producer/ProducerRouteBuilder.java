package com.example.amqp.producer;

import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.component.paho.mqtt5.PahoMqtt5Constants.CAMEL_PAHO_MSG_PROPERTIES;

import org.apache.camel.Header;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;
import org.springframework.stereotype.Component;


@Component
public class ProducerRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        from("timer:runOnce?repeatCount=1")
            .id("route-runOnce")
            .to("direct:sendMqtt");

        from("direct:sendMqtt")
            .id("route-paho-producer")
            .setBody(constant("HELLO from Camel!"))
            .setHeader(CAMEL_PAHO_MSG_PROPERTIES, method(this, "setTraceParent"))
            .log(INFO, "New message with trace=${header.traceparent}")
            .log("HEADERS ${in.headers}")
            .log("Exchange information: ${exchange}")
            .to("paho-mqtt5:{{app.topic}}");
    }

    public MqttProperties setTraceParent(
            @Header("traceparent") String traceParent,
            @Header(CAMEL_PAHO_MSG_PROPERTIES) MqttProperties mqttProperties) {
        mqttProperties = mqttProperties == null ? new MqttProperties() : mqttProperties;
        mqttProperties.getUserProperties().add(new UserProperty("traceparent", traceParent));
        return mqttProperties;
    }
}
