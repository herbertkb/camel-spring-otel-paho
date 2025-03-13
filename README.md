## Consumer
```
cd camel-spring-otel-paho-consumer
mvn clean spring-boot:run 
```

## Producer

Sends only once per run.
```
cd camel-spring-otel-paho-producer
mvn clean spring-boot:run 
```

## Jaeger

```
docker run --rm --name jaeger   -p 16686:16686   -p 4317:4317   -p 4318:4318   -p 5778:5778   -p 9411:9411   jaegertracing/jaeger:2.4.0
```

## Artemis

Create a default activemq-artemis instance and run it with user=`admin` and password=`password`.
