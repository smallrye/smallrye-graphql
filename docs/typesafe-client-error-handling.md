# Error handling in typesafe clients

## System-level errors


If the service returns a system-level error (which means the response body doesn't contain
a valid GraphQL response document), the client invocation will throw a
`io.smallrye.graphql.client.InvalidResponseException`
whose message contains information about the received status code, status message, and body
contents.

## Application-level errors

If the service returns one or more application-level errors (which means that
there is valid GraphQL response in the body that has a non-empty `errors` entry), 
the client normally throws a `GraphQLClientException` containing the details in a list of
`GraphQLClientError`.

> [NOTE]
> An error response is considered application-level regardless of the HTTP status code as long as it
> contains a valid GraphQL response. We define a GraphQL response as a well-formed JSON document
> that contains at least one of `errors`, `data` and `extensions` entries, and no other entries beyond
> that.

If the error is specific to a `location`, you can use an `ErrorOr`
wrapper on the target field; the client the maps the error to that
wrapper instead of throwing an exception. I.e. your `SuperHero` class
could look like this:

``` java
class SuperHero {
    String name;
    ErrorOr<Location> location;
}
```

If the service returns a response like this:

``` json
{
  "data": {
    "superHero": {
      "name": "Wolverine",
      "location": null
    }
  },
  "errors": [{
    "message":"location unknown",
    "path": ["superHero","location"],
    "extensions":{"code":"location-unknown"}
  }]
}
```

Then the `SuperHero#location` wrapper field will not contain a `value`
but only the error above. See the `ErrorOr` class for details.