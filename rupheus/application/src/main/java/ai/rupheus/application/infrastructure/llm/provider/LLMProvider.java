package ai.rupheus.application.infrastructure.llm.provider;

public interface LLMProvider {
    Class<?> getConfigClass();
    boolean testConnection(Object config);
}
