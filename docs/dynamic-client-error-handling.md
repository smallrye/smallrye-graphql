# Error handling with dynamic clients
                              

## System-level errors 

If the service returns a system-level error (which means the response body doesn't contain
a valid GraphQL response document), the client invocation will (instead of returning a 
`io.smallrye.graphql.client.Response`) throw a `io.smallrye.graphql.client.InvalidResponseException`
whose message contains information about the received status code, status message, and body
contents.

## Application-level errors

If the service returns one or more application-level errors (which means that
there is valid GraphQL response in the body that has a non-empty `errors` entry), the
client invocation will return a `io.smallrye.graphql.client.Response` as normal, and that object
will contain information about the errors. Errors are represented as `io.smallrye.graphql.client.GraphQLError`
objects and can be inspected after retrieving a list of all errors using `response.getErrors()`.

> [NOTE]
> An error response is considered application-level regardless of the HTTP status code as long as it
> contains a valid GraphQL response. We define a GraphQL response as a well-formed JSON document
> that contains at least one of `errors`, `data` and `extensions` entries, and no other entries beyond
> that.

It is also possible to convert a response's errors into a `io.smallrye.graphql.client.GraphQLClientException`
by calling `throwExceptionIfErrors()` - the response has to be cast to `ResponseImpl` for this as of now.
This method will, if there are any errors in the response, convert the errors into a `GraphQLClientException` 
and throw it.

If you need to access the HTTP response code and message pertaining to a response that contained
application-level errors, refer to [Accessing HTTP headers and response codes](dynamic-client-usage.md#accessing-http-headers-and-response-codes).