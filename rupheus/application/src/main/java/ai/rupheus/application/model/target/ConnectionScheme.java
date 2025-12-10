package ai.rupheus.application.model.target;

import ai.rupheus.application.infrastructure.llm.config.LocalModelConfig;
import ai.rupheus.application.infrastructure.llm.config.OpenAIConfig;
import lombok.Getter;

@Getter
public enum ConnectionScheme {
    OPENAI("provider.openai.base-url", OpenAIConfig.class),
    REST("provider.rest.base-url", LocalModelConfig.class);

    private final String configKey;
    private final Class<?> configClass;

    ConnectionScheme(String configKey, Class<?> configClass) {
        this.configKey = configKey;
        this.configClass = configClass;
    }
}
