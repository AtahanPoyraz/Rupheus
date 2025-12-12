package ai.rupheus.application.adapter.llm.provider;

import ai.rupheus.application.model.target.Provider;
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

    public LLMProvider resolve(Provider provider) {
        if (provider == null) {
            throw new IllegalArgumentException("connectionScheme can not be null");
        }

        return switch (provider) {
            case OPENAI -> this.openAIProvider;
            case LOCALMODEL ->  this.localModelProvider;
        };
    }
}
