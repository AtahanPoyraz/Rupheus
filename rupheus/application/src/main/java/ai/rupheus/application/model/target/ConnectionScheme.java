package ai.rupheus.application.model.target;

import ai.rupheus.application.infrastructure.llm.config.LocalModelConfig;
import ai.rupheus.application.infrastructure.llm.config.OpenAIConfig;
import lombok.Getter;

@Getter
public enum ConnectionScheme {
    OPENAI(OpenAIConfig.class),
    REST(LocalModelConfig.class);

    private final Class<?> configClass;

    ConnectionScheme(Class<?> configClass) {
        this.configClass = configClass;
    }
}
