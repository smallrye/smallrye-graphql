Dynamic client introduction and basic usage
===========

A Java GraphQL client. The main difference from the typesafe client is
that while the typesafe client behaves like a typesafe proxy very
similar to the MicroProfile REST Client, the dynamic client is more like
the JAX-RS client from the `javax.ws.rs.client` package. Instead of
working with model classes directly, the dynamic client focuses on
programmatically working with GraphQL documents representing GraphQL
requests and responses. It still offers the option to convert between
documents and model classes when necessary.

In the current implementation, Vert.x HTTP client is used for handling
the underlying traffic.

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

Initializing the client instance
================================

There are two ways to obtain a client instance.

Using CDI injection where the configuration values are defined in system
properties:
            
```java
@Inject
@NamedClient("superheroes")
DynamicGraphQLClient client;

// assuming that this system property exists:
// superheroes/mp-graphql/url=https://superheroes.org/graphql
```

Programmatically using a builder:

```java
DynamicGraphQLClient client = DynamicGraphQLClientBuilder.newBuilder()
    .url("https://superheroes.org/graphql")
    .build();
```

Configuration properties
========================

These properties apply when you’re using CDI to inject named client
instances (the first example in the previous section).

-   `CLIENT_NAME/mp-graphql/url` - defines the URL where the client
    should connect

-   `CLIENT_NAME/mp-graphql/header/KEY` - this property declares that
    the client will add a HTTP header named `KEY` to all requests (with
    a value being the value of this property)
