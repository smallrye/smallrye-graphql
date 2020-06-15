package io.smallrye.graphql;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SRGQL")
public interface SmallRyeGraphQLServerLogging {

    // numbers reserved for this module are 10000-19999

    SmallRyeGraphQLServerLogging log = Logger.getMessageLogger(SmallRyeGraphQLServerLogging.class,
            SmallRyeGraphQLServerLogging.class.getPackage().getName());

    /* 10000-10999: bootstrap related logs */

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10000, value = "Schema is null, or it has no operations. Not bootstrapping SmallRye GraphQL")
    void emptyOrNullSchema();

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 10001, value = "No GraphQL methods found. Try annotating your methods with @Query or @Mutation")
    void noGraphQLMethodsFound();

    /* 11000-11999: query related logs */

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 11000, value = "We got a String as input for Variables, not sure what to do with that [%s]")
    void stringInputForVariables(String stringVars);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 11001, value = "Retrieved from cache: %s")
    void retrievedFromCache(String query);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 11002, value = "Added to cache: %s")
    void addedToCache(String query);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 11003, value = "Cannot use the no-arg constructor to build instances of type %s")
    void noArgConstructorMissing(String typeName);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 11004, value = "Returning argument as is, because we did not know how to handle it [%s]")
    void dontKnowHoToHandleArgument(String name);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 11005, value = "Payload In [%s]")
    void payloadIn(String payload);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 11006, value = "Payload Out [%s]")
    void payloadOut(String payload);

    /* 12000-12999: data fetching related logs */

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 12000, value = "Data Fetching Error")
    void dataFetchingError(@Cause Throwable cause);

    /* 13000-13999: service related logs (CDI, Tracing, Metrics,...) */

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 13001, value = "Using %s lookup service for metrics")
    void usingMetricsService(String name);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 13002, value = "Using %s lookup service for tracing")
    void usingTracingService(String name);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 13003, value = "Using %s service for object lookups")
    void usingLookupService(String name);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 13004, value = "Using %s service for class loading")
    void usingClassLoadingService(String name);

    /* 14000-14999: data transforming related logs */

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 14000, value = "Unable to transform data")
    void transformError(@Cause Throwable t);

}
