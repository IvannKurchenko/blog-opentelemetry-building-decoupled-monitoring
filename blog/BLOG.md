OpenTelemetry: Building decoupled monitoring

### Short description
In this blog post, we will explore to use OpenTelemetry and OTLP protocol to build an observability infrastructure
easy to extend and decoupled from the application.

## Introduction
TODO
- What is OpenTelemetry in short
- What is OTLP in short?
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

## Links and further reading
Code source for this blog post can be found [here](TODO)
OpenTelemetry links:
- OTLP protocol;
- Supported languages;