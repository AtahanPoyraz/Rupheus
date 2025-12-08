package ai.rupheus.application.model.enums;

import ai.rupheus.application.model.pojos.LocalModelConfig;
import ai.rupheus.application.model.pojos.OpenAIConfig;
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
