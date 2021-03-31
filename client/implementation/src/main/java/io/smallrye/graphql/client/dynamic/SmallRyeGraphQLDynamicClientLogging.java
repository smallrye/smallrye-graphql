package io.smallrye.graphql.client.dynamic;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SRGQL")
public interface SmallRyeGraphQLDynamicClientLogging {

    // numbers reserved for this module are 30000-34999

    SmallRyeGraphQLDynamicClientLogging log = Logger.getMessageLogger(SmallRyeGraphQLDynamicClientLogging.class,
            SmallRyeGraphQLDynamicClientLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 30000, value = "Received response containing no data")
    void noDataInResponse();

}
