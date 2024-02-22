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


