package io.github.ivankurchenko.blogoteldecoupledmonitoring.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.client.ClientConfiguration;
import org.springframework.data.elasticsearch.client.elc.ElasticsearchConfiguration;

@Configuration
public class ApplicationElasticsearchConfiguration extends ElasticsearchConfiguration {

    private final Environment environment;

    public ApplicationElasticsearchConfiguration(Environment environment) {
        this.environment = environment;
    }

    @Override
    public ClientConfiguration clientConfiguration() {
        var uri = environment.getProperty("spring.elasticsearch.rest.uri");
        return ClientConfiguration.builder()
                .connectedTo(uri)
                .build();
    }
}
