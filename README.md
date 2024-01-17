# Blog: Opentelemetry: Building decoupled monitoring

## Blog
This repository contains source code for "@Opentelemetry: Building decoupled monitoring" blog post.

## Layout

### Subprojects
Main source code divided on following subprojects:
```
TODO
/ folder - description
```

### Misc
Apart from source code current repo has the following folders:
```
blog - blog post
docker - various docker compose files to setup  
```

## How to run
To run the system.

Build necessary docker containers:
```
TODO
```

Run system setup using docker compose. For instance :
```
docker-compose -f docker-TODO
```

After, run load testing to simulate user traffic
```
TODO
```

Check target APM or any monitoring tool and verify telemetry has been sent.

Stop environment using docker compose:
```
docker-compose -f TODO down
```

