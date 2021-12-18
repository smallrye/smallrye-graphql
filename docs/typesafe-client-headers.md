Adding headers
=======

To add a custom header to the http requests sent out by the GraphQL
Client, annotate your method or the API interface as `@Header`, e.g.:

``` java
@GraphQLClientApi
interface SuperHeroesApi {
    @Header(name = "S.H.I.E.L.D.-Clearance", constant = "TOP-SECRET")
    List<SuperHero> allHeroesIn(String location);
}
```

The value can be specified with a `constant` or the name of a `method` for dynamic values, e.g.:

``` java
@GraphQLClientApi
interface SuperHeroesApi {
    @Header(name = "S.H.I.E.L.D.-Clearance", method = "establishShieldClearance")
    List<SuperHero> allHeroesIn(String location);

    static Clearance establishShieldClearance() { 
        return userIsInRole(MANAGER) ? TOP_SECRET : PUBLIC;
    }
}
```

-   This example uses an enum, but it can be any Object; the GraphQL client
    calls `toString()` to convert it.

The method must be `static` and accessible by the interface, i.e. in the
interface itself or in one of the classes it’s nested in; if it’s in a
different class, prefix it with the fully qualified class name and a dot
`"."`, e.g.
`@Header(name = "S.H.I.E.L.D.-Clearance", method = "org.superheroes.SecurityTools.establishShieldClearance")`.

A third option is to pass the value of a header as an API method
parameter:

``` java
@GraphQLClientApi
interface SuperHeroesApi {
    List<SuperHero> allHeroesIn(String location, @Header(name = "S.H.I.E.L.D.-Clearance") Clearance clearance);
}
```

The `@Header` parameter will not be part of the GraphQL query.

The `name` of the header is always static, but can optionally be derived from the name of the parameter or method, i.e. if it has a `@Name` annotation, that annotation value is used. If it's not annotated, the method name (eventually stripped off a leading `get`) or the parameter name (if it's enabled in the compiler settings) is converted from camel case to kebab case, i.e. a parameter `@Header String requestIdentifier` will result in a header named `Request-Identifier`.

`@Header` annotations can also be defined via `@Stereotype`.

When instantiating the API with the builder, you can set (or overwrite)
one or more headers there:

``` java
SuperHeroesApi api = TypesafeGraphQLClientBuilder.newBuilder()
    .header("S.H.I.E.L.D.-Clearance", "TOP-SECRET")
    .build(SuperHeroesApi.class);
```


Authorization headers
====================

To add an `Authorization` header, instead of using the generic `@Header`
annotation, you can also use the special `@AuthorizationHeader`
annotation. It produces a `BASIC` `Authorization` header by default or a
`BEARER` token. You can configure the credentials in MP Config with a
prefix plus `/mp-graphql/` and either `username` and `password` for
`BASIC` or `bearer` for `BEARER`. The config key defaults to the fully
qualified name of the `GraphQLClientApi` interface or its `configKey`.

You can use a custom prefix by setting the `confPrefix`. The infix
`/mp-graphql/` is still applied, unless you end the `confPrefix` with
`*`, e.g.
`@AuthorizationHeader(confPrefix = "org.superheroes.security.basic.*`
will use `org.superheroes.security.basic.username`, while `*` will use
plain `username`.

`@AuthorizationHeader` annotations can be defined via `@Stereotype`.
