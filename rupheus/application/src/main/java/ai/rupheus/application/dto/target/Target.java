package ai.rupheus.application.dto.target;

import ai.rupheus.application.model.target.TargetModel;
import ai.rupheus.application.model.target.Provider;
import ai.rupheus.application.model.target.TargetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;

import java.time.LocalDateTime;
import java.util.*;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class Target {
    private UUID id;
    private String name;
    private String description;
    private Provider provider;
    private Map<String, Object> config;
    private TargetStatus status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static Target fromEntity(TargetModel targetModel) {
        Target target = new Target();
        target.setId(targetModel.getId());
        target.setName(targetModel.getName());
        target.setDescription(targetModel.getDescription());
        target.setProvider(targetModel.getProvider());
        target.setStatus(targetModel.getStatus());
        target.setCreatedAt(targetModel.getCreatedAt());
        target.setUpdatedAt(targetModel.getUpdatedAt());

        Map<String, Object> maskedConfig = null;
        if (targetModel.getConfig() != null) {
            maskedConfig = new HashMap<>(targetModel.getConfig());
            maskedConfig.computeIfPresent("apiKey", (k, v) -> "************");
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

    public static Page<Target> fromEntity(Page<TargetModel> targetModels) {
        List<Target> mapped = targetModels.getContent()
            .stream()
            .map(Target::fromEntity)
            .toList();

        return new PageImpl<>(
            mapped,
            targetModels.getPageable(),
            targetModels.getTotalElements()
        );
    }
}
