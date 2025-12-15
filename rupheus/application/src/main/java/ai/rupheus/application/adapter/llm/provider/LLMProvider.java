package ai.rupheus.application.adapter.llm.provider;

public interface LLMProvider {
    Class<?> getConfigClass();
    Object mergeConfig(Object existingConfig, Object incomingConfig);
    boolean isConnectionVerified(Object config);
}
