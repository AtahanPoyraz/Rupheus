package ai.rupheus.application.model.target;

import ai.rupheus.application.adapter.llm.config.LocalModelConfig;
import ai.rupheus.application.adapter.llm.config.OpenAIConfig;
import lombok.Getter;

@Getter
public enum ConnectionScheme {
    OPENAI(OpenAIConfig.class),
    LOCALMODEL(LocalModelConfig.class);

    private final Class<?> configClass;

    ConnectionScheme(Class<?> configClass) {
        this.configClass = configClass;
    }
}
