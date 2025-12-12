package ai.rupheus.application.adapter.llm.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalModelConfig {
    @NotEmpty(message = "Endpoint cannot be empty")
    private String endpoint;

    @NotEmpty(message = "Validate url cannot be empty")
    private String validateUrl;
    private String modelName;
    private String apiKey;
}
