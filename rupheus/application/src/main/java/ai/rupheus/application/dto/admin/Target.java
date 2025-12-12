package ai.rupheus.application.dto.admin;

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
        target.setConfig(targetModel.getConfig());
        return target;
    }

    public static List<Target> fromEntity(List<TargetModel> targetModels) {
        List<Target> targets = new ArrayList<>();
        for (TargetModel targetModel : targetModels) {
            targets.add(fromEntity(targetModel));
        }

        return targets;
    }

    public static PageableResponse<List<Target>> fromPage(Page<TargetModel> targetModelPage) {
        List<Target> targets = targetModelPage.getContent()
            .stream()
            .map(Target::fromEntity)
            .toList();

        PageableResponse<List<Target>> pageableResponse = new PageableResponse<>();
        pageableResponse.setContent(targets);

        PageableResponse.PageableInfo pageableInfo = new PageableResponse.PageableInfo();
        pageableInfo.setPage(targetModelPage.getNumber());
        pageableInfo.setSize(targetModelPage.getSize());
        pageableInfo.setTotalItems(targetModelPage.getTotalElements());
        pageableInfo.setTotalPages(targetModelPage.getTotalPages());

        pageableInfo.setHasNext(targetModelPage.hasNext());
        pageableInfo.setHasPrevious(targetModelPage.hasPrevious());
        pageableInfo.setFirst(targetModelPage.isFirst());
        pageableInfo.setLast(targetModelPage.isLast());

        PageableResponse.SortInfo sortInfo = new PageableResponse.SortInfo();
        sortInfo.setSorted(targetModelPage.getSort().isSorted());
        sortInfo.setUnsorted(targetModelPage.getSort().isUnsorted());
        sortInfo.setEmpty(targetModelPage.getSort().isEmpty());

        pageableInfo.setSort(sortInfo);

        pageableResponse.setPageable(pageableInfo);

        return pageableResponse;
    }
}
