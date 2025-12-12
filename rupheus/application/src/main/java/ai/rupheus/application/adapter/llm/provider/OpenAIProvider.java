package ai.rupheus.application.adapter.llm.provider;

import ai.rupheus.application.adapter.llm.config.OpenAIConfig;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpHeaders;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;

@Component
public class OpenAIProvider implements LLMProvider {
    @Value("${provider.openai.base_url}")
    private String baseUrl;

    private HttpClient httpClient;

    @PostConstruct
    private void init() {
        if (this.baseUrl == null || this.baseUrl.isEmpty()) {
            throw new IllegalStateException("OpenAI base url is empty");
        }

        this.httpClient = HttpClient.newBuilder()
            .version(HttpClient.Version.HTTP_2)
            .connectTimeout(java.time.Duration.ofSeconds(2))
            .build();
    }

    @Override
    public Class<?> getConfigClass() {
        return OpenAIConfig.class;
    }

    @Override
    public boolean testConnection(Object config) {
        OpenAIConfig openAIConfig = (OpenAIConfig) config;
        HttpRequest request = HttpRequest.newBuilder()
            .uri(URI.create(baseUrl + "/v1/models"))
            .header(HttpHeaders.AUTHORIZATION, "Bearer " + openAIConfig.getApiKey())
            .GET()
            .build();

        try {
            HttpResponse<Void> response =
                this.httpClient.send(request, HttpResponse.BodyHandlers.discarding());

            return response.statusCode() == 200;
        } catch (Exception e) {
            throw new IllegalStateException("An error occurred while validating OpenAI credentials: " + e.getMessage(), e);
        }
    }
}
