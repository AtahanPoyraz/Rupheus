package ai.rupheus.application.infrastructure.llm.provider;

import ai.rupheus.application.infrastructure.llm.config.LocalModelConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.WebClient;

@Component
public class LocalModelProvider implements LLMProvider {
    @Value("${provider.local_model.base_url}")
    private String baseUrl;

    private WebClient webClient;

    @PostConstruct
    private void init() {
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            throw new IllegalStateException("Local model base url is empty");
        }

        this.webClient = WebClient.builder()
                .baseUrl(this.baseUrl)
                .defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
                .build();
    }

    @Override
    public Class<?> getConfigClass() {
        return LocalModelConfig.class;
    }

    @Override
    public boolean testConnection(Object config) {
        LocalModelConfig localModelConfig = (LocalModelConfig) config;
        return true;
    }
}
