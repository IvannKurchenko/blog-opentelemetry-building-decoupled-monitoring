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
Show how to add a new monitoring system to the infrastructure - for Grafana.

https://grafana.com/docs/agent/latest/flow/tasks/collect-opentelemetry-data/#configure-an-opentelemetry-protocol-receiver -
configure OTLP receiver in Grafana Agent;
configure OTLP exporter to Grafana Agent;
configure Grafana Agent to export to Tempo, Mimir and Loki;

http://localhost:3000/explore?panes=%7B%22GwV%22:%7B%22datasource%22:%22tempo%22,%22queries%22:%5B%7B%22refId%22:%22A%22,%22datasource%22:%7B%22type%22:%22tempo%22,%22uid%22:%22tempo%22%7D,%22queryType%22:%22traceqlSearch%22,%22limit%22:20,%22tableType%22:%22traces%22,%22filters%22:%5B%7B%22id%22:%22949bb2c6%22,%22operator%22:%22%3D%22,%22scope%22:%22span%22%7D,%7B%22id%22:%22min-duration%22,%22tag%22:%22duration%22,%22operator%22:%22%3E%22,%22valueType%22:%22duration%22,%22value%22:%2250ms%22%7D%5D,%22groupBy%22:%5B%7B%22id%22:%2299afbe22%22,%22scope%22:%22span%22%7D%5D%7D%5D,%22range%22:%7B%22from%22:%22now-6h%22,%22to%22:%22now%22%7D%7D%7D&schemaVersion=1&orgId=1 -
Grafana Tempo - trace search example;

Note - Grafana has also Mimir - newest solution for metrics, but 
at the time of writing this blog post it did not support OTLP protocol.
https://grafana.com/oss/mimir/



## Processing signals
As extra we can show how to manage signals - metrics, traces and logs - e.g. filter, transform, aggregate etc. using
collector processors.

https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/filterprocessor - filter processor;
show example - filter traces and metrics for health checks
https://opentelemetry.io/docs/collector/transforming-telemetry/#basic-filtering

https://github.com/open-telemetry/opentelemetry-collector-contrib/tree/main/processor/attributesprocessor
- attributes processor;
Add attribute - environment=prod for all signals.
  https://opentelemetry.io/docs/collector/transforming-telemetry/#adding-or-deleting-attributes

NOTE - PROCESSORS AT THE MOMENT OF WRITING ARE ALPHA VERSION AND SUBJECT TO CHANGE!

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

https://medium.com/@gleydsoncavalcanti/sending-traces-with-the-grafana-agent-for-grafana-tempo-4092b25c35d0
https://github.com/grafana/intro-to-mltp - grafana stack demo example
https://grafana.com/docs/mimir/latest/get-started/ - grafana mimir quick startup example;