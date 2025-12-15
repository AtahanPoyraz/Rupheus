package ai.rupheus.application.dto.target;

import ai.rupheus.application.dto.shared.PageableResponse;
import ai.rupheus.application.model.target.Provider;
import ai.rupheus.application.model.target.TargetModel;
import ai.rupheus.application.model.target.TargetStatus;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.data.domain.Page;

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
        target.setConfig(maskConfig(targetModel.getConfig()));
        return target;
    }

    public static List<Target> fromEntity(List<TargetModel> targetModels) {
        return targetModels.stream()
            .map(Target::fromEntity)
            .toList();
    }

    public static PageableResponse<List<Target>> fromPage(Page<TargetModel> page) {
        PageableResponse<List<Target>> response = new PageableResponse<>();
        response.setContent(fromEntity(page.getContent()));

        PageableResponse.PageableInfo pageableInfo = new PageableResponse.PageableInfo();
        pageableInfo.setPage(page.getNumber());
        pageableInfo.setSize(page.getSize());
        pageableInfo.setTotalItems(page.getTotalElements());
        pageableInfo.setTotalPages(page.getTotalPages());
        pageableInfo.setHasNext(page.hasNext());
        pageableInfo.setHasPrevious(page.hasPrevious());
        pageableInfo.setFirst(page.isFirst());
        pageableInfo.setLast(page.isLast());

        PageableResponse.SortInfo sortInfo = new PageableResponse.SortInfo();
        sortInfo.setSorted(page.getSort().isSorted());
        sortInfo.setUnsorted(page.getSort().isUnsorted());
        sortInfo.setEmpty(page.getSort().isEmpty());

        pageableInfo.setSort(sortInfo);
        response.setPageable(pageableInfo);

        return response;
    }

    private static Map<String, Object> maskConfig(Map<String, Object> config) {
        if (config == null || config.isEmpty()) {
            return null;
        }

        Map<String, Object> masked = new HashMap<>(config);
        masked.computeIfPresent("apiKey", (k, v) -> maskApiKey(v));
        return masked;
    }

    private static String maskApiKey(Object value) {
        String key = value.toString();
        if (key.length() <= 6) {
            return "******";
        }
        return key.substring(0, 3) + "****" + key.substring(key.length() - 3);
    }
}
