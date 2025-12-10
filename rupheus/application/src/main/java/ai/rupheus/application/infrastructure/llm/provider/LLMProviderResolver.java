package ai.rupheus.application.infrastructure.llm.provider;

import ai.rupheus.application.model.target.ConnectionScheme;
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

    public LLMProvider resolve(ConnectionScheme connectionScheme) {
        if (connectionScheme == null) {
            throw new IllegalArgumentException("connectionScheme can not be null");
        }

        return switch (connectionScheme) {
            case OPENAI -> this.openAIProvider;
            case REST ->  this.localModelProvider;
        };
    }
}
