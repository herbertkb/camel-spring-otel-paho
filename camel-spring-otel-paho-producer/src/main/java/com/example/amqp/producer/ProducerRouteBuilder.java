package com.example.amqp.producer;

import static org.apache.camel.LoggingLevel.INFO;
import static org.apache.camel.component.paho.mqtt5.PahoMqtt5Constants.CAMEL_PAHO_MSG_PROPERTIES;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import org.apache.camel.Headers;
import org.apache.camel.builder.RouteBuilder;
import org.eclipse.paho.mqttv5.common.packet.MqttProperties;
import org.eclipse.paho.mqttv5.common.packet.UserProperty;
import org.springframework.stereotype.Component;


@Component
public class ProducerRouteBuilder extends RouteBuilder {

    @Override
    public void configure() throws Exception {

        // restConfiguration()
        //     .component("servlet");

        // rest()
        //     .get("/test")
        //     .to("direct:sendMqtt");

        from("timer:runOnce?repeatCount=1")
            .id("route-runOnce")
            .to("direct:sendMqtt");

        from("direct:sendMqtt")
            .id("route-paho-producer")
            .setBody(constant("HELLO from Camel!"))
            .setHeader(CAMEL_PAHO_MSG_PROPERTIES, method(this, "mqttPropertiesFromHeaders"))
            .log(INFO, "New message with trace=${header.traceparent}")
            .log("HEADERS ${in.headers}")
            .log("Exchange information: ${exchange}")
            .to("paho-mqtt5:{{app.topic}}");
    }

    public MqttProperties mqttPropertiesFromHeaders(@Headers Map<String, Object> headers) {
        List<UserProperty> userProperties = headers.entrySet().stream()
                .map(entry -> new UserProperty(entry.getKey(), String.valueOf(entry.getValue())))
                .collect(Collectors.toList());
        
        MqttProperties properties = new MqttProperties();
        properties.setUserProperties(userProperties);

        return properties;
    }
}
