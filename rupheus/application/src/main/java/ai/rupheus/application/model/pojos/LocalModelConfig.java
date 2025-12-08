package ai.rupheus.application.model.pojos;

import jakarta.validation.constraints.NotEmpty;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class LocalModelConfig {
    @NotEmpty(message = "Base url cannot be empty")
    private String baseUrl;

    @NotEmpty(message = "Endpoint be empty")
    private String endpoint;
    private String modelName;
    private Double temperature;
    private Integer maxTokens;
    private Double topP;
    private Integer topK;
    private Double repetitionPenalty;
    private String apiKey;
}
