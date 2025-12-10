package ai.rupheus.application.infrastructure.llm.provider;

import ai.rupheus.application.model.target.ConnectionScheme;
import org.springframework.core.env.Environment;
import org.springframework.stereotype.Component;

@Component
public class ProviderUrlResolver {
    private final Environment env;

    public ProviderUrlResolver(Environment env) {
        this.env = env;
    }

    public String resolve(ConnectionScheme scheme) {
        return env.getProperty(scheme.getConfigKey());
    }
}
