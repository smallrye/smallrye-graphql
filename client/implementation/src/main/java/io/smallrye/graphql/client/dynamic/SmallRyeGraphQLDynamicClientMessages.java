package io.smallrye.graphql.client.dynamic;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

import io.smallrye.graphql.client.dynamic.api.DynamicClientException;

@MessageBundle(projectCode = "SRGQLDC")
public interface SmallRyeGraphQLDynamicClientMessages {

    SmallRyeGraphQLDynamicClientMessages msg = Messages.getBundle(SmallRyeGraphQLDynamicClientMessages.class);

    // numbers reserved for this module are 35000-39999

    @Message(id = 35000, value = "Cannot parse response: %s")
    DynamicClientException cannotParseResponse(String response);

}
