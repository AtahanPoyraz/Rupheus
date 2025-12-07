package ai.rupheus.application.model.enums;

public enum ConnectionScheme {
    OPENAI("provider.openai.base-url"),
    CLAUDE("provider.claude.base-url"),
    HUGGINGFACE("provider.huggingface.base-url"),
    OPENROUTER("provider.openrouter.base-url"),
    REST("provider.rest.base-url");

    private final String configKey;

    ConnectionScheme(String configKey) {
        this.configKey = configKey;
    }

    public String getConfigKey() {
        return configKey;
    }
}
