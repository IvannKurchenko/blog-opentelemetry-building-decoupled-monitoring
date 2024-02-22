# Building decoupled monitoring with OpenTelemetry

### Short description
In this blog post, we will explore how to use OpenTelemetry Collector to build an observability infrastructure easy to extend and decoupled from the application.

## Introduction
[OpenTelemetry](https://opentelemetry.io/docs/), as documentation says, is:
> OpenTelemetry, also known as OTel, is a vendor-neutral open source Observability framework for instrumenting, generating, collecting, and exporting telemetry data such as traces, metrics, and logs.

It covers a wide range of observability aspects: application instrumentation, telemetry data processing and publishing to other telemetry backends.
However, it is important to mention that OpenTelemetry itself is not a telemetry storage or visualization tool.
There are plenty of other systems that provide those capabilities, such as commercial APMs like Datadog or open source tools like Zipkin. 

To cover all these processes, OTEL provides a number of components to build a complete observability system:
- [Language APIs & SDKs](https://opentelemetry.io/docs/languages/) — a set of consistent SDKs for many popular languages and respective frameworks. 
Along with instrumentation that allows to automatically collect telemetry data for most common cases like HTTP requests handling or databases querying.
- [Collector](https://opentelemetry.io/docs/collector/) - OpenTelemetry Collector is a piece of infrastructure software that essentially provides capabilities to build an [ETL](https://en.wikipedia.org/wiki/Extract,_transform,_load) process for all kinds of telemetry data. 

Although software components are crucial, it is also important to mention the basis it is built on top of.
In particular, I'd like to mention two specifications:
- [Open Telemetry Protocol](https://opentelemetry.io/docs/specs/otlp/) - this is a gRPC, push-based protocol. Own protocol is a one of the OTELs pillars that allow it to be vendor-neutral. This protocol covers all transportation of all sorts telemetry data that [OTEL handles](https://opentelemetry.io/docs/concepts/signals/): baggage, metrics, traces and logs.
- [Semantic conversions](https://opentelemetry.io/docs/specs/semconv/) - set of standard notions for telemetry data. For instance, any HTTP framework instrumentation needs to produce the same metrics with same attributes as it described in [HTTP Sever Metrics](https://opentelemetry.io/docs/specs/semconv/http/http-metrics/#metric-httpserverrequestduration) part of specification.   

We will not do a deep inside these specs, but it is worth briefly mentioning them for better further understanding.


## System under monitoring
To showcase the capabilities of OpenTelemetry, let's first build a simple service to monitor.
This is going to be a simple HTTP REST service for online shop product management, that provides CRUD API for simple products with name, description and price.
To make it a bit more interesting, let's also add full text search capabilities, so we can have more complex telemetry data.
Product service architecture can be described with the following diagram:
![1-system-under-monitoring.png](images%2F1-system-under-monitoring.png)

And its API examples:
```http request
# Create product. Response body example for 200 OK:
# {
#  "id": 3,
#  "name": "Test product",
#  "description": "Test Description",
#  "price": 100
#}
POST localhost:8080/api/product
Content-Type: application/json

{
  "name": "Test product",
  "description": "Test Description",
  "price": 100.00
}

###
# Get product. Response body example for 200 OK:
# {
#  "id": 1,
#  "name": "Test product",
#  "description": "Test Description",
#  "price": 100
# }
GET localhost:8080/api/product/1

###
# Search product. # Response body example for 200 OK:
# [
#    {
#        "id": 1,
#        "name": "Test product",
#        "description": "Test Description",
#        "price": 100
#    }
# ]
GET localhost:8080/api/product?query=Test

###
# Delete product. Response body for 200 OK is empty.
DELETE localhost:8080/api/product/1
```

To produce some telemetry data out of application, we need to simulate traffic.
For this, we will have [a simple script](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/main/traffic.sh)
to create, read, search and delete 100 products sequentially.

Although This service is implemented in Java and Spring Boot, please, bear in mind that approaches shown further can be applied to any language, framework or other software that leverages OpenTelemetry to produce telemetry signals.   

## Direct publishing
We have an application that we want to monitor. In the simplest case, we can plug the instrumentation and supply configuration to send telemetry data directly to backends.
One of the versions of the described setup might look in the following way:

![2-direct-publishing.png](images%2F2-direct-publishing.png)

As it is shown in the diagram, OpenTelemetry instrumentation can directly send metrics to [Prometheus](https://prometheus.io) and traces to [Jaeger](https://www.jaegertracing.io).
In the case of Java or any other JVM language, we can pick default [OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation) and use in the service Docker like so:
```dockerfile
# Download OpenTelemetry
ARG OTEL_VERSION=v2.0.0
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/$OTEL_VERSION/opentelemetry-javaagent.jar

#...

# Add java agnet to enable telemetry instrumentation
ENTRYPOINT ["java","-javaagent:opentelemetry-javaagent.jar","-jar","/usr/app/products-service.jar"]
```

Some details are omitted. Full Dockerfile can be found at [the GitHub repository](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/main/java/Dockerfile).
Once the instrumentation is plugged, it can be configured with the following environment variables to expose `9094` port to scrape metrics by Prometheus and send traces to Jaeger:  
```shell
OTEL_SERVICE_NAME=products_service
OTEL_METRICS_EXPORTER=prometheus
OTEL_EXPORTER_PROMETHEUS_PORT=9094
OTEL_EXPORTER_PROMETHEUS_HOST=0.0.0.0
OTEL_TRACES_EXPORTER=otlp
OTEL_EXPORTER_OTLP_ENDPOINT=http://jaeger:4318
```

You can find in the GitHub repository [Docker compose file](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/master/docker-compose/setup-direct-publishing.yaml) to run this setup.
Let run the whole setup and run the script to simulate traffic. Firstly, to check metrics in Prometheus open http://localhost:9090/graph in your browser.
We can use the following PromQL query to calculate average response latency: `sum(http_server_request_duration_seconds_sum) / sum(http_server_request_duration_seconds_count)`

You can observe something like in the following screenshot:
![3-direct-publishing-prometheus.png](images%2F3-direct-publishing-prometheus.png)

To check the trace data, open Jaeger at http://localhost:16686, search for traces for Service `products_service` and Operation `POST /api/product`.
![3-direct-publishing-jaeger-search.png](images%2F3-direct-publishing-jaeger-search.png)

We can open of the traces to check details and inner spans:
![3-direct-publishing-jaeger-trace.png](images%2F3-direct-publishing-jaeger-trace.png)

## Publishing to a collector
Since our