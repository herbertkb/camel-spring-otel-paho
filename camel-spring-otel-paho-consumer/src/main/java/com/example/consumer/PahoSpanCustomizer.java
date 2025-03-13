// package com.example.consumer;

// import org.apache.camel.Exchange;

// import io.opentelemetry.api.trace.SpanBuilder;
// import org.apache.camel.opentelemetry.SpanCustomizer;

// public class PahoSpanCustomizer implements SpanCustomizer {
//     @Override
//     public void customize(SpanBuilder spanBuilder, String operationName, Exchange exchange) {
//         spanBuilder.setAttribute("foo", "bar");
//         spanBuilder.setParent(myCustomParentContext);
//         spanBuilder.addLink(linkedSpanContext);
//         // other customizations...
//     }

//     // // By default, all spans will have customizations applied. You can restrict this by overriding isEnabled
//     // @Override
//     // public boolean isEnabled(String operationName, Exchange exchange) {
//     //     String header = exchange.getMessage().getHeader("foo", String.class);
//     //     return operationName.equals("my-message-queue") && header.equals("bar");
//     // }
// }