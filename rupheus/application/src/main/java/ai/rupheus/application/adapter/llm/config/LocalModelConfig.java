package ai.rupheus.application.adapter.llm.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalModelConfig {
    private String url;
    private String validationEndpoint;
    private String apiKey;
    private String model;
    private Double temperature;
    private Integer maxToken;
    private String systemPrompt;
}
