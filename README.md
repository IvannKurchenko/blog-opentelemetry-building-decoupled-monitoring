# Blog "Building decoupled monitoring with OpenTelemetry"

This repository contains source code for "Building decoupled monitoring with OpenTelemetry" blog post.

## Layout
The repository is organized as follows:
```
./blog - directory containing blog post content
./docker-compose - dirrectory containing docker-compose files for running various system setups
./docker-compose/mount - directory containing files to be mounted to docker containers.
./java - directory containing java source code for the example application
.requests.http - file containing HTTP requests as an example of how to interact with the example application
.traffic.sh - shell script to simulate user traffic
```

## How to run

## Prerequisites
Before running any system setup, you need to have the following tools installed:
- Docker
- Docker Compose

## Build the example application
Before running any system setup, you need to build the example application:
```
cd java
docker build -t products-service .
```

## Run direct publishing
This is the simplest monitoring setup. The idea behind this setup is to demonstrate how to send telemetry directly from the application to the monitoring system backend using OpenTelemetry.
In particular, the application sends traces to Jaeger and metrics to Prometheus.
To run it, use the following commands:
```
cd docker-compose
docker-compose -f docker-compose/setup-direct-publishing.yaml up -d
```
You can check whether application is running by sending a request `GET localhost:8080/health` or by checking status of the docker container `products-service`.
After the system is up and running, run [traffic.sh](traffic.sh) to simulate user traffic.

After that, you can check telemetry data in:
- Prometheus. Open http://localhost:9090. You can use the following query to see application average latency: `sum(products_service_http_server_request_duration_seconds_sum) / sum(products_service_http_server_request_duration_seconds_count)` 
- Jaeger. Open http://localhost:16686. You can choose `products-service` from the list of services and see traces.

Stop environment using docker compose:
```
docker-compose -f docker-compose/setup-direct-publishing.yaml down
```

## Run publishing though OpenTelemetry Collector
This setup demonstrates how to send telemetry data to the monitoring system backend using OpenTelemetry Collector.
It still sends traces to Jaeger and metrics to Prometheus, but the application sends telemetry data to OpenTelemetry Collector, which then forwards it to the monitoring system backend.
To run it, use the following commands:
```
cd docker-compose
docker-compose -f docker-compose/setup-otel-collector-publishing.yaml up -d
```

You can check application status, send traffic and check telemetry data as described in the previous section.

Stop environment using docker compose:
```
docker-compose -f docker-compose/setup-otel-collector-publishing.yaml down
```

## Run publishing through OpenTelemetry Collector to multiple backends
This setup demonstrates how to send telemetry data to multiple monitoring system backends using OpenTelemetry Collector.
It sends the following telemetry data to the monitoring system backends:
- Traces to Jaeger and Grafana Agent, which in its turn sends traces to Tempo.
- Metrics to Prometheus and Grafana Agent, which in its turn sends metrics to Mimir.
- Logs to Grafana Agent, which in its turn sends logs to Loki.

To run it, use the following commands:
```
cd docker-compose
docker-compose -f docker-compose/setup-otel-collector-publishing-extended.yaml up -d
```

You can check application status, send traffic as described in the previous section.
Check Grafana stack health in Grafana Agent UI: http://localhost:12345. All components should show "Healthy" status.

Additionally, to the previous setups, you can check telemetry data in:
- Tempo. Open Grana UI http://localhost:3000 and go to Explore. Choose "Tempo" as a data source, click on "Run Query" see traces.
- Mimir. Open Grana UI http://localhost:3000 and go to Explore. Choose "Mimir" as a data source, use Prometheus like query to find metrics. For instance: `http_server_request_duration_seconds_bucket{http_route="/api/product"}`
- Loki. Open Grana UI http://localhost:3000 and go to Explore. Choose "Loki" as a data source, use Loki like query to find logs. For instance: `{job="products_service"}`

Stop environment using docker compose:
```
docker-compose -f docker-compose/setup-otel-collector-publishing-extended.yaml down
```

## Run publishing through OpenTelemetry Collector to multiple backends with telemetry data processing.
This setup demonstrates how to send telemetry data to multiple monitoring system backends using OpenTelemetry Collector and process it before sending.
It sends telemetry data to the same backends as the previous setup, but before sending it, it processes telemetry data using OpenTelemetry Collector processors:
- It filters out all traces for `GET /health` requests.
- It filters out all logs containing `Health check API invoked!` line in the log line body.
- It adds a new attribute `environment` to all telemetry data with the value `development`.

To run it, use the following commands:
```
cd docker-compose
docker-compose -f docker-compose/setup-otel-collector-publishing-extended-processed.yaml up -d
```

You can check application status, send traffic as described in the previous section.
You can check telemetry data in the same way as in the previous setup.

Make sure there are no logs containing `Health check API invoked!` line in the log line body in Loki using the following query: `{job="products_service"} |~ "Health check API invoked!.*"`
Make sure there are no traces for `GET /health` requests in Tempo.

Stop environment using docker compose:
```
docker-compose -f docker-compose/setup-otel-collector-publishing-extended-processed.yaml down
```