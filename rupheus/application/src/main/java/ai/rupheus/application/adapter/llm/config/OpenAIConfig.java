package ai.rupheus.application.adapter.llm.config;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenAIConfig {
    private String apiKey;
    private String model;
    private Double temperature;
    private Integer maxToken;
    private String systemPrompt;
}
