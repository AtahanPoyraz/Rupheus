package ai.rupheus.application.dto.admin;

import ai.rupheus.application.model.enums.TargetStatus;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;
import java.util.UUID;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class CreateTargetRequest {
    @NotBlank(message = "Target name cannot be empty")
    private String targetName;

    @NotBlank(message = "Target description cannot be empty")
    private String targetDescription;

    @NotNull(message = "Target config cannot be empty")
    private Map<String, Object> config;

    @NotBlank(message = "User id cannot be empty")
    private UUID userId;
}
