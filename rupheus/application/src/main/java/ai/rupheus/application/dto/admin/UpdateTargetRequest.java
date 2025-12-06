package ai.rupheus.application.dto.admin;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTargetRequest {
    @NotBlank(message = "Target name cannot be empty")
    private String targetName;

    @NotBlank(message = "Target description cannot be empty")
    private String targetDescription;

    @NotNull(message = "Target config cannot be empty")
    private Map<String, Object> config;
}
