package ai.rupheus.application.dto.admin;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class UpdateTargetRequest {
    private String targetName;
    private String targetDescription;
    private Map<String, Object> config;
}
