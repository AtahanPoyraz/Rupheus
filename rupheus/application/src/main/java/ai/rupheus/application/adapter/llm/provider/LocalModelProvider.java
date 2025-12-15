package ai.rupheus.application.adapter.llm.provider;

import ai.rupheus.application.adapter.llm.config.LocalModelConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class LocalModelProvider implements LLMProvider {
    @Value("${provider.local_model.base_url}")
    private String baseUrl;

    private HttpClient httpClient;

    @PostConstruct
    private void init() {
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            throw new IllegalStateException("Local model base url is empty");
        }

        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(java.time.Duration.ofSeconds(2))
            .build();
    }

    @Override
    public Class<?> getConfigClass() {
        return LocalModelConfig.class;
    }

    @Override
    public Object mergeConfig(Object existingConfig, Object incomingConfig) {
        LocalModelConfig existing = (LocalModelConfig) existingConfig;
        LocalModelConfig incoming = (LocalModelConfig) incomingConfig;

        if (incoming.getApiKey() != null && !incoming.getApiKey().isEmpty()) {
            existing.setApiKey(incoming.getApiKey());
        }

        if (incoming.getUrl() != null && !incoming.getUrl().isEmpty()) {
            existing.setUrl(incoming.getUrl());
        }

        if (incoming.getValidationEndpoint() != null && !incoming.getValidationEndpoint().isEmpty()) {
            existing.setValidationEndpoint(incoming.getValidationEndpoint());
        }

        if (incoming.getModel() != null && !incoming.getModel().isEmpty()) {
            existing.setModel(incoming.getModel());
        }

        if (incoming.getTemperature() != null) {
            existing.setTemperature(incoming.getTemperature());
        }

        if (incoming.getMaxToken() != null) {
            existing.setMaxToken(incoming.getMaxToken());
        }

        if (incoming.getSystemPrompt() != null && !incoming.getSystemPrompt().isEmpty()) {
            existing.setSystemPrompt(incoming.getSystemPrompt());
        }

        return existing;
    }

    @Override
    public boolean isConnectionVerified(Object config) {
        LocalModelConfig localModelConfig = (LocalModelConfig) config;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(localModelConfig.getUrl() + localModelConfig.getValidationEndpoint()))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + localModelConfig.getApiKey())
            .GET()
            .build();

        try {
            HttpResponse<Void> response =
                this.httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            return response.statusCode() == 200;
        } catch (Exception e) {
            throw new IllegalStateException("An error occurred while validating Local Model credentials: " + e.getMessage(), e);
        }
    }
}
