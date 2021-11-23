Error handling in typesafe clients
======

If the service returns one or more errors, the client normally throws a
`GraphQLClientException` containing the details in a list of
`GraphQLClientError`.

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