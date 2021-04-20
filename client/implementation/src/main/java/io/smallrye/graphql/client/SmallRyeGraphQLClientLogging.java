package io.smallrye.graphql.client;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SRGQL")
public interface SmallRyeGraphQLClientLogging {

    // numbers reserved for this module are 30000-34999

    SmallRyeGraphQLClientLogging log = Logger.getMessageLogger(SmallRyeGraphQLClientLogging.class,
            SmallRyeGraphQLClientLogging.class.getPackage().getName());

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 30000, value = "Received response containing no data")
    void noDataInResponse();

}
