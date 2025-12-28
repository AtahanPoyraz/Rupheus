package ai.rupheus.application.adapter.llm.provider;

import ai.rupheus.application.model.target.TargetProvider;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class LLMProviderResolver {
    private final OpenAIProvider openAIProvider;
    private final LocalModelProvider localModelProvider;

    @Autowired
    public LLMProviderResolver(
        OpenAIProvider openAIProvider,
        LocalModelProvider localModelProvider
    ) {
        this.openAIProvider = openAIProvider;
        this.localModelProvider = localModelProvider;
    }

    public LLMProvider resolve(TargetProvider targetProvider) {
        if (targetProvider == null) {
            throw new IllegalArgumentException("Provider cannot be null");
        }

        return switch (targetProvider) {
            case OPENAI -> this.openAIProvider;
            case LOCALMODEL -> this.localModelProvider;
        };
    }
}
