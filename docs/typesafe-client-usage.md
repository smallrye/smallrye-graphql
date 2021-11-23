A Java code-first type-safe GraphQL Client API suggestion for
[Microprofile GraphQL Issue
\#185](https://github.com/eclipse/microprofile-graphql/issues/185).

Basic Usage
===========
      
Creating the client-side counterpart of the GraphQL API:

```java
--8<-- "docs/snippets/examples/typesafeclient/SuperHeroesApi.java"
```

A model class:

```java
--8<-- "docs/snippets/examples/typesafeclient/SuperHero.java"
```

Injecting the client using CDI and using it:

```java
--8<-- "docs/snippets/examples/typesafeclient/MyClientUsage.java"
```

-   The default request type is `query`. To make it a mutation, annotate
    it `@Mutation`. The parameter name is only available if you compile
    the source with the `-parameters` option. Otherwise, you’ll have to
    annotate all parameters with `@Name`.

The example above uses CDI, e.g. when you are in a MicroProfile or
Jakarta EE environment. If you are in an environment without CDI
support, you need to instantiate the API interface by using the builder:

``` java
SuperHeroesApi api = TypesafeGraphQLClientBuilder.newBuilder().build(SuperHeroesApi.class);
```

The basic idea of the Java code-first approach is that you start by
writing the DTOs and query/mutation methods as you need them in your
client. This ensures that you don’t request fields that you don’t need;
the thinking is inspired by [Consumer Driven
Contracts](https://martinfowler.com/articles/consumerDrivenContracts.html).


If the server uses names different from yours, you can simply use
annotations to do a mapping:

Name Mapping / Aliases
======================

If the server defines a different field or parameter name, annotate it
with `@Name`. If the server defines a different query name, annotate the
method as, e.g., `@Query("findHeroesCurrentlyLocatedIn")`.

By renaming methods, you can also define several variations of the same
request but using different return types or parameters. E.g.:

``` java
public interface SuperHeroesApi {
    SuperHero findHeroByName(String name); 

    @Query("findHeroByName")
    SuperHeroWithTeams findHeroWithTeamsByName(String name); 
}
```

-   The `SuperHero` class has no team affiliations (for this example).

-   The `SuperHeroWithTeams` class has a `List<Team> teamAffiliations`
    field. The actual query name is still `findHeroByName`. The `Team`
    class doesn’t contain the members to break recursion.

If you rename a field or method, the real field or method name will be
used as an alias, so you can select the same data twice (see `` and ``
below).

Configuration
=============

If the endpoint is always the same, e.g. a public API of a cloud
service, you can add the URL to your API annotation, e.g.:

``` java
@GraphQLClientApi(endpoint = "https://superheroes.org/graphql")
interface SuperHeroesApi {
}
```

When instantiating the API with the builder, you can set (or overwrite)
the endpoint there:

``` java
SuperHeroesApi api = TypesafeGraphQLClientBuilder.newBuilder()
    .endpoint("https://superheroes.org/graphql")
    .build(SuperHeroesApi.class);
```

Commonly you’ll need different endpoints, e.g. when you need one
endpoint for your production system, but a different endpoint for your
test system. Simply use [MicroProfile
Config](https://download.eclipse.org/microprofile/microprofile-config-1.4/microprofile-config-spec.html)
to set the endpoint; similar to the [MicroProfile Rest
Client](https://download.eclipse.org/microprofile/microprofile-rest-client-1.4.1/microprofile-rest-client-1.4.1.html),
the key for the endpoint is the fully qualified name of the api
interface, plus `/mp-graphql/url`, e.g.:

``` properties
org.superheroes.SuperHeroesApi/mp-graphql/url=https://superheroes.org/graphql
```

If you want to use a different key, set the base config key on the
annotation `@GraphQLClientApi(configKey = "superheroes")`; then use this
key for the endpoint `superheroes/mp-graphql/url`.

When using the builder, you can override the config key as well:
`TypesafeGraphQLClientBuilder.newBuilder().configKey("superheroes")`.

NestedParameter
===============

Some APIs require parameters beyond the root level, e.g. for filtering
or paginating nested lists. Say you have a schema like this:

``` graphql
type Query {
    team(name: String!): Team!
}

type Team {
    members(first: Int!): [SuperHero!]!
}
```

To pass the parameter to the nested field/method, annotate it as
`@NestedParameter`, e.g.:

``` java
@GraphQLClientApi
interface TeamsApi {
    Team team(String name, @NestedParameter("members") int first);
}
```

The value of the `@NestedParameter` annotation is the dot-delimited path
to the nested field/method that the value should be added to.

