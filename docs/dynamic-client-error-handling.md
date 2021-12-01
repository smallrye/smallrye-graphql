# Error handling with dynamic clients

When executing GraphQL operations using a dynamic client, the application receives back an
instance of `io.smallrye.graphql.client.Response` that encapsulates the whole response from the service,
including potential errors. Errors are represented as `io.smallrye.graphql.client.GraphQLError`
and can be inspected after retrieving a list of all errors using `response.getErrors()`.

It is also possible to convert a response's errors into a `io.smallrye.graphql.client.GraphQLClientException`
by calling `throwExceptionIfErrors()` - the response has to be cast to `ResponseImpl` for this as of now.
This method will, if there are any errors in the response, convert the errors into a `GraphQLClientException` 
and throw it.