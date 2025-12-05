package ai.rupheus.application.dto.target;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTargetRequest {
    @NotBlank(message = "Target name cannot be empty")
    private String targetName;

    @NotBlank(message = "Target description cannot be empty")
    private String targetDescription;

    @NotBlank(message = "Target config cannot be empty")
    private Map<String, Object> config;
}
