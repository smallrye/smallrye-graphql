package io.smallrye.graphql.servlet;

import java.io.Reader;
import java.net.URL;

import org.jboss.logging.Logger;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.LogMessage;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageLogger;

@MessageLogger(projectCode = "SRGQL")
public interface SmallRyeGraphQLServletLogging {

    // numbers reserved for this module are 20000-29999

    SmallRyeGraphQLServletLogging log = Logger.getMessageLogger(SmallRyeGraphQLServletLogging.class,
            SmallRyeGraphQLServletLogging.class.getPackage().getName());

    /* 20000-20999: bootstrap related logs */

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 20000, value = "Loaded index from [%s]")
    void loadedIndexFrom(String path);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 20001, value = "No jandex index available, let's generate one...")
    void generatingIndex();

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 20002, value = "Processing file [%s]")
    void processingFile(String path);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 20003, value = "Cannot process file [%s]")
    void cannotProcessFile(String path, @Cause Exception cause);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 20004, value = "Cannot create URL from a JAR/WAR file included in the classpath")
    void cannotCreateUrl(@Cause Exception e);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 20005, value = "Ignoring url [%s] as it's not a jar, war or folder")
    void ignoringUrl(URL url);

    @LogMessage(level = Logger.Level.WARN)
    @Message(id = 20006, value = "Could not process url [%s] while indexing files")
    void couldNotProcessUrl(URL url, @Cause Exception e);

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 20007, value = "SmallRye GraphQL initialized")
    void initialized();

    @LogMessage(level = Logger.Level.INFO)
    @Message(id = 20008, value = "SmallRye GraphQL destroyed")
    void destroyed();

    /* 21000-21999: query processing related logs */

    @LogMessage(level = Logger.Level.ERROR)
    @Message(id = 21000, value = "IO Exception occurred")
    void ioException(@Cause Exception ex);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 21001, value = "JSON input: %s")
    void jsonInput(String input);

    @LogMessage(level = Logger.Level.DEBUG)
    @Message(id = 21002, value = "Unable to log reader %s")
    void unableToLogReader(Reader reader);

}
