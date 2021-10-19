package io.smallrye.graphql.client;

import javax.json.JsonValue;

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

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 30001, value = "The runtime did not set the `apis` field in `GraphQLClientsConfiguration`, so the bean " +
            "is unable to locate configurations of typesafe clients")
    void apisNotSet();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 30002, value = "Unsupported path segment type %s (expecting only String or Number), ignoring")
    void unknownPathSegmentType(JsonValue.ValueType type);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 30003, value = "Unsupported extension type %s, ignoring")
    void unknownExtensionType(JsonValue.ValueType valueType);

}
