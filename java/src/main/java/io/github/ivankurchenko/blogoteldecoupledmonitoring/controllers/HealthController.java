package io.github.ivankurchenko.blogoteldecoupledmonitoring.controllers;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class HealthController {
    private static final Logger logger = LoggerFactory.getLogger(HealthController.class);

    @GetMapping("/health")
    public String health() {
        logger.info("Health check API invoked!");
        return "OK";
    }
}
