package ai.rupheus.application.infrastructure.llm.provider;

import ai.rupheus.application.infrastructure.llm.config.OpenAIConfig;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class OpenAIProvider implements LLMProvider {
    @Value("${provider.openai.base_url}")
    private String baseUrl;

    @Override
    public Class<?> getConfigClass() {
        return OpenAIConfig.class;
    }

    @Override
    public boolean testConnection(Object config) {
        return true;
    }
}
