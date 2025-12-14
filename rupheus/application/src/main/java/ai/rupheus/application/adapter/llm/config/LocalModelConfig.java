package ai.rupheus.application.adapter.llm.config;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalModelConfig {
    @NotEmpty(message = "Url cannot be empty")
    private String url;

    @NotEmpty(message = "Validation endpoint cannot be empty")
    private String validationEndpoint;
    private String modelName;
    private String apiKey;
}
