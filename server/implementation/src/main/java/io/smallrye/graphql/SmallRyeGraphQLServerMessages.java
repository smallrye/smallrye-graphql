package io.smallrye.graphql;

import static java.lang.invoke.MethodHandles.lookup;

import java.time.DateTimeException;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

import graphql.schema.CoercingParseLiteralException;
import graphql.schema.CoercingParseValueException;
import graphql.schema.CoercingSerializeException;
import io.smallrye.graphql.execution.datafetcher.DataFetcherException;
import io.smallrye.graphql.execution.resolver.ConcreteImplementationNotFoundException;
import io.smallrye.graphql.schema.model.Operation;

@MessageBundle(projectCode = "SRGQL")
public interface SmallRyeGraphQLServerMessages {

    SmallRyeGraphQLServerMessages msg = Messages.getBundle(lookup(), SmallRyeGraphQLServerMessages.class);

    @Message(id = 0, value = "No concrete class named [%s] found for interface [%s]")
    ConcreteImplementationNotFoundException concreteClassNotFoundForInterface(String clazz, String iface);

    @Message(id = 1, value = "Unknown primitive type [%s]")
    ClassNotFoundException unknownPrimitiveType(String name);

    @Message(id = 2, value = "Data fetching failed for operation [%s]")
    DataFetcherException dataFetcherException(Operation operation, @Cause Throwable cause);

    @Message(id = 3, value = "Data fetching failed for [%s]")
    DataFetcherException generalDataFetcherException(String operation, @Cause Throwable cause);

    @Message(id = 5, value = "Could not get Instance using the default lookup service")
    RuntimeException countNotGetInstance(@Cause Throwable t);

    @Message(id = 6, value = "Metrics are not supported without CDI")
    UnsupportedOperationException metricsNotSupportedWithoutCDI();

    @Message(id = 8, value = "OpenTracing is not supported without CDI")
    UnsupportedOperationException openTracingNotSupportedWithoutCDI();

    @Message(id = 9, value = "Can not load class [%s]")
    RuntimeException canNotLoadClass(String className, @Cause Exception cause);

    @Message(id = 10, value = "[%s] is not a valid number type")
    RuntimeException notAValidNumberType(String typeClassName);

    @Message(id = 11, value = "Can not parse a number from [%s]")
    NumberFormatException numberFormatException(String input);

    @Message(id = 12, value = "Expected type [%s] but was [%s].")
    CoercingSerializeException coercingSerializeException(String expectedType, String actualType, @Cause Exception cause);

    @Message(id = 13, value = "Expected type [%s] but was [%s].")
    CoercingParseValueException coercingParseValueException(String expectedType, String actualType, @Cause Exception cause);

    @Message(id = 14, value = "Expected value to be in the %s range but it was '%s'")
    CoercingParseLiteralException coercingParseLiteralException(String expectedRange, String actual);

    @Message(id = 15, value = "Expected AST type 'IntValue' or 'StringValue' but was '%s'.")
    CoercingParseLiteralException coercingParseLiteralException(String actualType);

    @Message(id = 16, value = "Can't parse [%s] into [%s]")
    RuntimeException cantParseDate(String inputTypeName, String targetClassName);

    @Message(id = 17, value = "[%s] is no valid date or time-type")
    RuntimeException notValidDateOrTimeType(String className);

    @Message(id = 18, value = "Unknown date format [%s]")
    DateTimeException unknownDateFormat(String input);

    @Message(id = 19, value = "Unsupported wrapped type. SmallRye only support DataFetchingEnvironment and not %s")
    IllegalArgumentException unsupportedWrappedClass(String className);

    @Message(id = 20, value = "Can not inject an instance of class [%s]. Please make sure it is a CDI bean, also possibly the beans.xml file is needed")
    RuntimeException canNotInjectClass(String className, @Cause Exception cause);

    @Message(id = 21, value = "Can not parse a integer from [%s]")
    CoercingParseLiteralException integerCoercingParseException(String input);

    @Message(id = 22, value = "Can not parse a number from [%s]")
    CoercingParseLiteralException numberCoercingParseException(String input);
}
