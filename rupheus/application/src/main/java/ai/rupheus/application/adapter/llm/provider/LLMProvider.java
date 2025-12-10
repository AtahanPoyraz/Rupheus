package ai.rupheus.application.adapter.llm.provider;

public interface LLMProvider {
    Class<?> getConfigClass();
    boolean testConnection(Object config);
}
