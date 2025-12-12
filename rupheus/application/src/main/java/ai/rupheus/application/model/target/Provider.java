package ai.rupheus.application.model.target;

import ai.rupheus.application.adapter.llm.config.LocalModelConfig;
import ai.rupheus.application.adapter.llm.config.OpenAIConfig;
import lombok.Getter;

@Getter
public enum Provider {
    OPENAI(OpenAIConfig.class),
    LOCALMODEL(LocalModelConfig.class);

    private final Class<?> configClass;

    Provider(Class<?> configClass) {
        this.configClass = configClass;
    }
}
