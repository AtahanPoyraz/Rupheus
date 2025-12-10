package ai.rupheus.application.adapter.llm.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class OpenAIConfig {
    @NotEmpty(message = "Api key cannot be empty")
    private String apiKey;

    @NotEmpty(message = "Model cannot be empty")
    private String model;
    private Double temperature;
    private Integer maxTokens;
}
