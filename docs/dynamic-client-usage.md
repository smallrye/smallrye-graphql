Dynamic client introduction and basic usage
===========

A Java GraphQL client. The main difference from the typesafe client is
that while the typesafe client behaves like a typesafe proxy very
similar to the MicroProfile REST Client, the dynamic client is more like
the JAX-RS client from the `jakarta.ws.rs.client` package. Instead of
working with model classes directly, the dynamic client focuses on
programmatically working with GraphQL documents representing GraphQL
requests and responses. It still offers the option to convert between
documents and model classes when necessary.

In the current implementation, Vert.x HTTP client is used for handling
the underlying traffic.

Creating a client instance
==========================

Generally there are two ways to obtain a client instance.

First, using CDI injection where the configuration values are defined in system
properties:

```java
@Inject
@GraphQLClient("superheroes")
DynamicGraphQLClient client;

// assuming that this system property exists:
// superheroes/mp-graphql/url=https://superheroes.org/graphql
```

The above example assumes that configuration for the client is present in system properties. For a full list of
supported configuration properties, see [Client configuration reference](client_configuration.md)

The other way to build a client is programmatically using a builder:

```java
DynamicGraphQLClient client = DynamicGraphQLClientBuilder.newBuilder()
    .url("https://superheroes.org/graphql")
    .build();
```

The usage examples in the following sections will assume using the first approach - injection.

Basic Usage
===========

Given the following GraphQL service on the server side:

``` java
@GraphQLApi
class SuperHeroesApi {
    @Query
    List<SuperHero> allHeroesIn(String location) {
        // ....
    }
}

class SuperHero {
    private String name;
    private List<String> superPowers;
}
```

Such service can be queried this way:

``` java
--8<-- "docs/snippets/examples/dynamicclient/MyClientUsage.java"
```

-   <1> Creating the document representing the request. We used static
    imports to make the code easy to read, they all come from the
    classes in the `io.smallrye.graphql.client.core` package.

-   <2> Executing the request. You can either do that in a blocking way, or
    request a `Uni` if you prefer the reactive style.

-   <3> Obtaining the resulting list of heroes as a `JsonArray`.

-   <4> Obtaining the resulting list of heroes as instances of the model
    class. This is optional, you can continue working with the data as a `JsonArray` if you prefer.
    
Using plain strings instead of the DSL
======================================

If you don't like the DSL for some reason and want to use plain strings for your queries, these two examples
will serve you:

``` java
--8<-- "docs/snippets/examples/dynamicclient/MyClientUsageString.java"
```

- <1>: In this variant, we inline values for query arguments directly into the query string.

- <2>: In this variant, argument values are extracted into variables. The `location` argument of the `allHeroesIn` query is 
    declared to be using the variable `loc` (the variable can also be named `location` same as the argument, if
    you prefer).

- <3>: Here we create a map that defines the values for each variable. Values of this map are `Object`s,
    so you can put in strings, numbers, booleans, or any object that corresponds to a GraphQL type and can be
    serialized to JSON. Inserting a `JsonObject` directly is also supported.

Accessing HTTP headers and response codes
=========================================

To access HTTP transport metadata that was passed by the server, you can inspect the `Response` object.

`Response.getTransportMeta("HEADER-NAME")` returns a `List<String>` containing (potentially multiple) values of the requested header.

To get the HTTP status code or status message, these are stored inside the `transportMeta` map too. `ResponseImpl` 
contains convenience methods to retrieve them: `ResponseImpl.getStatusCode()` and `ResponseImpl.getStatusMessage()`.
It's also possible to retrieve them directly without casting to `ResponseImpl` by calling
`Integer.valueOf(response.getTransportMeta().get("<status-code>").get(0))` and  
`response.getTransportMeta().get("<status-message>").get(0)`.

HTTP headers, status codes and messages are only available for operations executed over pure HTTP, not via websockets!

