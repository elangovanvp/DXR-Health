package com.dxr.health.config;

import com.dxr.health.ai.MockTriageModelAdapter;
import com.dxr.health.ai.RemoteInferenceAdapter;
import com.dxr.health.ai.TriageModelAdapter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;

@Configuration
public class AiAdapterConfig {

    @Bean
    @Primary
    public TriageModelAdapter primaryTriageAdapter(
            @Value("${ai.adapter.type:mock}") String adapterType,
            MockTriageModelAdapter mockAdapter,
            RemoteInferenceAdapter remoteAdapter
    ) {
        if ("remote".equalsIgnoreCase(adapterType)) {
            return remoteAdapter;
        }
        return mockAdapter;
    }
}
