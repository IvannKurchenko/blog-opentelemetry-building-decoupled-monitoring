# Building decoupled monitoring with OpenTelemetry

### Short description
In this blog post, we will explore how to use OpenTelemetry Collector to build an observability infrastructure easy to extend and decoupled from the application.

## Introduction
[OpenTelemetry](https://opentelemetry.io/docs/), as documentation says, is:
> OpenTelemetry, also known as OTel, is a vendor-neutral open source Observability framework for instrumenting, generating, collecting, and exporting telemetry data such as traces, metrics, and logs.
In further text, we will refer to OpenTelemetry as OTEL.

It covers a wide range of observability aspects: application instrumentation, telemetry data processing and publishing to other telemetry backends.
Although OpenTelemetry excels in these areas, it's designed to integrate with, rather than replace, telemetry storage and visualization tools.  
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
The diagram shows how OpenTelemetry instrumentation directly sends metrics to Prometheus and traces to Jaeger:

![2-direct-publishing.png](images%2F2-direct-publishing.png)

As it is shown in the diagram, OpenTelemetry instrumentation can directly send metrics to [Prometheus](https://prometheus.io) and traces to [Jaeger](https://www.jaegertracing.io).
In the case of Java or any other JVM language, we can pick the default [OpenTelemetry Java agent](https://github.com/open-telemetry/opentelemetry-java-instrumentation) and use in the service Docker like so:
```dockerfile
# Download the OpenTelemetry Java agent for instrumentation. 
ARG OTEL_VERSION=v2.0.0
RUN wget https://github.com/open-telemetry/opentelemetry-java-instrumentation/releases/download/$OTEL_VERSION/opentelemetry-javaagent.jar

#...

# Add java agnet to enable telemetry instrumentation.
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
Now we have a telemetry infrastructure for the Products Service up and running. However, the world is evolving over time and 
there might be a need to migrate to other monitoring systems or add new ones. In case of a single application, it might be not a big deal, but in case of a big system with many services, this might be a serious challenge.
This is one of the cases when OpenTelemetry Collector comes to the rescue. It can be used to decouple the application from the monitoring infrastructure and provide a single point of configuration for all telemetry data.
OpenTelemetry collector works with OTLP protocol, mentioned in the introduction, and can be configured to export telemetry data to various backends.
A collector usually consists of the following components:
- [Receivers](https://opentelemetry.io/docs/collector/configuration/#receivers) - describe how to receive telemetry data (e.g. extract);
- [Processors](https://opentelemetry.io/docs/collector/configuration/#processors) - describe how to process telemetry data (e.g. transform);
- [Exporters](https://opentelemetry.io/docs/collector/configuration/#exporters) - describe how to export telemetry data (e.g. load).;
- [Connectors](https://opentelemetry.io/docs/collector/configuration/#exporters) - describe how to connect telemetry data pipelines. Not covered here;
- [Extensions](https://opentelemetry.io/docs/collector/configuration/#extensions) - describe how to extend the collector with additional capabilities. Not covered here;

So to decouple the application from the monitoring infrastructure, this collector needs to do is to receive telemetry data from the application and send metrics to the Prometheus and traces to Jaeger as before.
In terms of components, it means that the collector needs to have OTLP receiver and exporters for Prometheus and Jaeger.

Such a setup might look like the following diagram:
![4-publishing-via-collector.png](images%2F4-publishing-via-collector.png)

This can be achieved with the following configuration:
```yaml
receivers:
  otlp:
    protocols:
      http:
        endpoint: 0.0.0.0:4418

exporters:
  otlphttp:
    endpoint: http://jaeger:4318

  prometheus:
    endpoint: 0.0.0.0:9094
    namespace: products_service

service:
  pipelines:
    traces:
      receivers: [otlp]
      exporters: [otlphttp]
    metrics:
      receivers: [otlp]
      exporters: [prometheus]
```
which later can be supplied to the collector as a configuration file for instance in a Docker compose:
```yaml
otel-collector:
  image: otel/opentelemetry-collector-contrib:0.93.0
  hostname: otel-collector
  volumes:
    - ./mount/otel-collector-config-prometheus-jaeger.yaml:/etc/otelcol-contrib/config.yaml
  ports:
    - "9094:9094" # Prometheus http exporter
    - "4418:4418" # OTLP http receiver
```

After this, we need to update the application configuration to send telemetry data to the collector:
```shell
OTEL_SERVICE_NAME=products_service
OTEL_METRICS_EXPORTER=otlp
OTEL_TRACES_EXPORTER=otlp
OTEL_EXPORTER_OTLP_ENDPOINT=http://otel-collector:4418
```

You can find in the GitHub repository [Docker compose file](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/master/docker-compose/setup-direct-publishing.yaml) to run this setup.
After running the setup, you can run the script to simulate traffic again and observe same results as in the previous section.

## Extending monitoring system
Since we introduced OpenTelemetry Collector to the infrastructure, it is easier to extend the monitoring system with new backends.
For instance, we might want to have a single place to view and analyze all telemetry data. Some sort of Application Monitoring System (APM), such as commercial solutions like Datadog or open source ones like Grafana.
Grafana OSS provides a set of tools to build a complete observability system also known as [LGTM stack](https://grafana.com/go/observabilitycon/2022/lgtm-scale-observability-with-mimir-loki-and-tempo/):
- [Loki](https://grafana.com/docs/loki/latest/) - a logs storage;
- [Grafana](https://grafana.com/docs/grafana/latest/) - a tool for visualization and analysis;
- [Tempo](https://grafana.com/docs/tempo/latest/) - a traces storage;
- [Mimir](https://grafana.com/docs/mimir/latest/) - a metrics storage;

Another crucial component in this stack is [Grafana Agent](https://grafana.com/docs/agent/latest/) which responsible for many things like collecting telemetry data and sending it to the backends.
In our case, this agent can be configured to receive telemetry data from the OpenTelemetry Collector and send it to LGTM stack components.
Such setup can be shown in the following diagram:
![5-extended-system.png](images%2F5-extended-system.png)

To achieve this, we need to add a new exporter to the OpenTelemetry Collector configuration:
```yaml
receivers:
  otlp:
    protocols:
      http:
        endpoint: 0.0.0.0:4418

exporters:
  otlphttp/jaeger:
    endpoint: http://jaeger:4318

  otlphttp/grafana-agent:
    endpoint: http://grafana-agent:4318

  prometheus:
    endpoint: 0.0.0.0:9094
    namespace: products_service

service:
  pipelines:
    traces:
      receivers: [otlp]
      exporters: [otlphttp/jaeger,otlphttp/grafana-agent]
    metrics:
      receivers: [otlp]
      exporters: [prometheus,otlphttp/grafana-agent]
    logs:
      receivers: [otlp]
      exporters: [otlphttp/grafana-agent]
```

As you can see, we configured a new exporter `otlphttp/grafana-agent` that is added in `service` section to the `traces`, `metrics` and `logs` pipelines.
Aside not here: pay attention to exporter names as they should be unique and follow the pattern `{protocol}/{name}`. Exporter name can be omitted if there is only one exporter for the protocol.
For sake of simplicity, we omitted the configuration of Grafana Agent and LGTM stack components, but you can find it in the GitHub repository:
- [Docker compose file](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/master/docker-compose/setup-otel-collector-publishing-extended.yaml)
- [Grafana Agent configuration](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/master/docker-compose/mount/grafana-agent.river)
- [Mimir configuration](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/main/docker-compose/mount/grafana-mimir.yaml)
- [Tempo configuration](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/main/docker-compose/mount/grafana-tempo.yaml)
- [Loki configuration](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/main/docker-compose/mount/grafana-loki.yaml)
- [Grafana configuration](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/main/docker-compose/mount/grafana-datasources.yaml)

After running the setup, you can run the script to simulate traffic again.
To check the metrics in Grafana, open http://localhost:3000, go to the "Explore" section and choose "Mimir" at the dropdown. We can use similar PrompQL query for testing: `sum(products_service_http_server_request_duration_seconds_sum) / sum(products_service_http_server_request_duration_seconds_count)`
Resulting graph could look something like this:
![5-extended-system-mimir.png](images%2F5-extended-system-mimir.png)
In the same Grafana, we can find traces in Tempo. Open http://localhost:3000, go to the "Explore" section and choose "Tempo" at the dropdown. You can search for traces for "Resource Service Name" equals to `products_service` and "Span Name" `POST /api/product`.
In the result search list you can pick any trace to see details and inner spans. The resulting trace could look something like this:
![5-extended-system-tempo.png](images%2F5-extended-system-tempo.png)

Now we can check logs in Loki. Open http://localhost:3000, go to the "Explore" section and choose "Loki" at the dropdown. 
Let's search log lines containing `Creating product` message that is produced by the product service on product creation. The resulting log lines could look something like this:
![5-extended-system-loki.png](images%2F5-extended-system-loki.png)

We extended the monitoring infrastructure without touching the application, only by changing the OpenTelemetry Collector configuration.

## Processing telemetry data
As it was mentioned before, OpenTelemetry Collector can receive, process and export telemetry data. It was shown how to configure it to receive and export telemetry data, hence it is worth mentioning how to process it.

OpenTelemetry provides a number of processors which provides reach possibilities for telemetry filtering, dimension modifications, batching, etc. Please, see for more details the following [documentation](https://opentelemetry.io/docs/collector/configuration/#processors).

The Products Service implements a simple health check API used by Docker to verify whether the application is ready to serve traffic. This is `GET /health` endpoint, that writes `Health check API invoked!` log on every call. This API is not interesting for monitoring, and we might want to filter it out.
[Filter processor](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/filterprocessor) is a perfect fit for this task.

Also lets imaging that there might be multiple environments, and we want to add an attribute to all telemetry data to distinguish them. [Attributes Processor](https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/attributesprocessor) can help with the requirement.

We can add these processors to the OpenTelemetry Collector configuration in the following way:
```yaml
receivers:
  otlp:
    protocols:
      http:
        endpoint: 0.0.0.0:4418

processors:
  # Filter HTTP spans to server for `GET /health` requests because of spam.
  filter/exclude-health-api-traces:
    error_mode: ignore
    traces:
      span:
        - 'attributes["http.route"] == "/health"'

  # Filter logs for `GET /health` requests logs because of spam.
  filter/exclude-health-api-logs:
    logs:
      exclude:
        match_type: regexp
        bodies:
          - '.*Health check API invoked!.*'

  # Add environment attribute to all telemetry signals.
  attributes/add-environment:
    actions:
      - key: environment
        value: development
        action: insert

exporters:
  otlphttp/jaeger:
    endpoint: http://jaeger:4318

  otlphttp/grafana-agent:
    endpoint: http://grafana-agent:4318

  prometheus:
    endpoint: 0.0.0.0:9094
    namespace: products_service

service:
  pipelines:
    traces:
      receivers:
        - otlp
      processors:
        - filter/exclude-health-api-traces
        - attributes/add-environment
      exporters:
        - otlphttp/jaeger
        - otlphttp/grafana-agent
    metrics:
      receivers:
        - otlp
      processors:
        - attributes/add-environment
      exporters:
        - prometheus
        - otlphttp/grafana-agent
    logs:
      receivers:
        - otlp
      processors:
        - filter/exclude-health-api-logs
        - attributes/add-environment
      exporters:
        - otlphttp/grafana-agent
```

You can find in the GitHub repository [Docker compose file](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring/blob/master/docker-compose/setup-otel-collector-publishing-extended-process.yaml) to run this setup.
After running the setup, you can run the script to simulate traffic again.
To check weather logs for `GET /health` requests are filtered out, open Loki in Grafana and search for log lines containing `Health check API invoked!` message. You should not see any log lines containing this message:
![6-filter-telemetry-loki.png](images%2F6-filter-telemetry-loki.png)

In Tempo, you can search for traces using span tags `http.route` equals to `/health` and you should not see any traces for this route:
![6-filter-telemetry-tempo.png](images%2F6-filter-telemetry-tempo.png)

Along with this you can check logs in to see that there is a new `environment` label with `development` value:
![6-process-telemetry-loki.png](images%2F6-process-telemetry-loki.png)

And pick any trace to see same new `environment` tag:
![6-process-telemetry-tempo.png](images%2F6-process-telemetry-tempo.png)

## Conclusion
OpenTelemetry is a great framework to build a complete observability system. 
Although it has a pretty wide number of tools, bear in mind that some of them are in "alpha" or "beta" stage and subject to change.

## References
- [GitHub repository with all the code](https://github.com/IvannKurchenko/blog-opentelemetry-building-decoupled-monitoring);
- [OTEL collector documentation](https://opentelemetry.io/docs/collector/);
- [OTEL recommended Processors](https://github.com/open-telemetry/opentelemetry-collector/tree/main/processor#recommended-processors);
- [OTEL Registry](https://opentelemetry.io/ecosystem/registry/) - find instrumentation for your language or framework and any other components;
- [Spring Boot instrumentation](https://opentelemetry.io/docs/languages/java/automatic/spring-boot/);
- [Introducing native support for OpenTelemetry in Jaeger](https://medium.com/jaegertracing/introducing-native-support-for-opentelemetry-in-jaeger-eb661be8183c);
- [Sending Traces with the Grafana Agent for Grafana Tempo](https://medium.com/@gleydsoncavalcanti/sending-traces-with-the-grafana-agent-for-grafana-tempo-4092b25c35d0);
- [Introduction to Metrics, Logs, Traces and Profiles in Grafana](https://github.com/grafana/intro-to-mltp);