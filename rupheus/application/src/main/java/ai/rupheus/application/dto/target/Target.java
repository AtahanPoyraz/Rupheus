package ai.rupheus.application.dto.target;

import ai.rupheus.application.model.TargetModel;
import ai.rupheus.application.model.enums.ConnectionScheme;
import ai.rupheus.application.model.enums.TargetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Target {
    private UUID id;
    private String name;
    private String description;
    private ConnectionScheme connectionScheme;
    private Map<String, Object> config;
    private TargetStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Target fromEntity(TargetModel targetModel) {
        Target target = new Target();
        target.setId(targetModel.getId());
        target.setName(targetModel.getName());
        target.setDescription(targetModel.getDescription());
        target.setConnectionScheme(targetModel.getScheme());
        target.setStatus(targetModel.getStatus());
        target.setCreatedAt(targetModel.getCreatedAt());
        target.setUpdatedAt(targetModel.getUpdatedAt());

        Map<String, Object> maskedConfig = null;
        if (targetModel.getConfig() != null) {
            maskedConfig = new HashMap<>(targetModel.getConfig());
            maskedConfig.computeIfPresent("apiKey", (k, v) -> "*".repeat(v.toString().length()));
        }

        target.setConfig(maskedConfig);
        return target;
    }

    public static List<Target> fromEntity(List<TargetModel> targetModels) {
        List<Target> targets = new ArrayList<>();
        for (TargetModel targetModel : targetModels) {
            targets.add(fromEntity(targetModel));
        }

        return targets;
    }
}
