package io.smallrye.graphql.client;

public class DefaultErrorMessageProvider implements ErrorMessageProvider {

    @Override
    public RuntimeException urlMissingErrorForNamedClient(String name) {
        return SmallRyeGraphQLClientMessages.msg.urlNotConfiguredForNamedClient(name);
    }

}
