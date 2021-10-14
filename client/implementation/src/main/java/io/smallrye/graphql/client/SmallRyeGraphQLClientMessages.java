package io.smallrye.graphql.client;

import java.util.NoSuchElementException;
import java.util.Set;

import org.jboss.logging.Messages;
import org.jboss.logging.annotations.Cause;
import org.jboss.logging.annotations.Message;
import org.jboss.logging.annotations.MessageBundle;

@MessageBundle(projectCode = "SRGQLDC")
public interface SmallRyeGraphQLClientMessages {

    SmallRyeGraphQLClientMessages msg = Messages.getBundle(SmallRyeGraphQLClientMessages.class);

    // numbers reserved for this module are 35000-39999

    @Message(id = 35000, value = "Cannot parse response: %s")
    RuntimeException cannotParseResponse(String response, @Cause Throwable cause);

    @Message(id = 35001, value = "URL not configured for client. Please define the property %s/mp-graphql/url or pass it to " +
            "your client builder dynamically")
    NoSuchElementException urlNotConfiguredForNamedClient(String name);

    @Message(id = 35002, value = "URL not configured for client. Please pass a url using your client builder.")
    NoSuchElementException urlNotConfiguredForProgrammaticClient();

    @Message(id = 35003, value = "Response did not contain any data")
    NoSuchElementException noDataInResponse();

    @Message(id = 35004, value = "Field %s not found in response's data. Available fields are: %s")
    NoSuchElementException fieldNotFoundInResponse(String field, Set<String> availableFields);

    @Message(id = 35005, value = "Value in the response at path '%s' is of an unexpected type: %s")
    RuntimeException unexpectedValueInResponse(String path, String value);

    @Message(id = 35006, value = "Field %s in the response is a single object, please use the method `getObject` instead of `getList`")
    IllegalArgumentException responseContainsSingleObject(String field);

    @Message(id = 35007, value = "Field %s in the response is an array, please use the method `getList` instead of `getObject`")
    IllegalArgumentException responseContainsArray(String field);

    @Message(id = 35008, value = "Field recursion found")
    IllegalStateException fieldRecursionFound();

    @Message(id = 35010, value = "Cannot instantiate %s")
    RuntimeException cannotInstantiateDomainObject(String object, @Cause Throwable t);

    @Message(id = 35011, value = "The response does not contain any data for operation %s")
    RuntimeException missingDataForOperation(String operation);

}
