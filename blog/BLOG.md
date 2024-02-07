OpenTelemetry: Building decoupled monitoring

### Short description
In this blog post, we will explore to use OpenTelemetry and OTLP protocol to build an observability infrastructure
easy to extend and decoupled from the application.

## Introduction
TODO
- What is OpenTelemetry in short
- What is OTLP in short?
- What is OTEL Colletor in short? https://opentelemetry.io/docs/concepts/components/#collector - definition.
- Blog post purpose - show how to use OpenTelemetry and its protocol to decouple the monitoring infrastructure 
from the application.

OpenTelemetry is a set of tools, APIs and SDKs used to instrument, generate, collect and export telemetry data
and not a storage or visualization solution.

## System under monitoring
To showcase the capabilities of OpenTelemetry lets first build a simple service to monitor.
TODO: 
- products-service - REST API to manage search products; (PG for storage and S3 for images);
- Load tests using wrk to simulate traffic; 

Languages used: Java and Python.
![1-system-under-monitoring.png](images%2F1-system-under-monitoring.png)

## Direct publishing
![2-direct-publishing.png](images%2F2-direct-publishing.png)

Direct publishing to an underlying monitoring system
TODO:
- How to use OpenTelemetry to publish metrics directly to a monitoring system - Prometheus;
- How to use OpenTelemetry to publish traces directly to a monitoring system - Jaeger;
- How to use OpenTelemetry to publish logs directly to a monitoring system - ELK;

Show diagram and code snippets.

Prometheus query:
`sum(products_service_http_server_request_duration_seconds_sum) / sum(products_service_http_server_request_duration_seconds_count)`

## Publishing to a collector 
TODO:
- What is OTLP protocol in details;
- What is a OTLP collector and exporter etc;
- How to use OpenTelemetry to publish metrics to a collector;
- How to use OpenTelemetry to publish traces to a collector;
- How to use OpenTelemetry to publish logs to a collector;

Lets take Grafana stack as for monitoring infrastructure:

Configure the collector to export:
- Metrics to Mimir;
- Traces to Tempo;
- Logs to Loki;

UI is Grafana.

Show diagram and code snippets.

## Extending monitoring system
Show how to add a new monitoring system to the infrastructure - for Signoz.

## Managing signals
As extra we can show how to manage signals - metrics, traces and logs - e.g. filter, transform, aggregate etc. using
collector processors.

https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/filterprocessor - filter processor;
show example - filter traces and metrics for health checks

Add attribute - environment=prod for all signals.

## Links and further reading
Code source for this blog post can be found [here](TODO)
OpenTelemetry links:
- OTLP protocol;
- Supported languages;

References:

https://opentelemetry.io/docs/collector/ - OTEL collector documentation;
https://opentelemetry.io/docs/kubernetes/helm/ - OTEL helm charts;

https://github.com/open-telemetry/opentelemetry-collector/tree/main/processor#recommended-processors - recommended processors;
https://opentelemetry.io/ecosystem/registry/?language=collector - OTEL collector extensions registry;

https://opentelemetry.io/docs/languages/java/automatic/spring-boot/ - Spring Boot instrumentation
https://medium.com/jaegertracing/introducing-native-support-for-opentelemetry-in-jaeger-eb661be8183c - used to configure Jaeger
https://github.com/open-telemetry/opentelemetry-java-instrumentation/blob/main/docs/supported-libraries.md#libraries--frameworks-

https://opentelemetry.io/docs/languages/java/automatic/spring-boot/