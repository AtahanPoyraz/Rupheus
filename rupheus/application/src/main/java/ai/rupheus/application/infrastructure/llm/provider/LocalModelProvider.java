package ai.rupheus.application.infrastructure.llm.provider;

import ai.rupheus.application.infrastructure.llm.config.LocalModelConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class LocalModelProvider implements LLMProvider {
    @Value("${provider.local_model.base_url}")
    private String baseUrl;

    @Override
    public Class<?> getConfigClass() {
        return LocalModelConfig.class;
    }

    @Override
    public boolean testConnection(Object config) {
        return true;
    }
}
