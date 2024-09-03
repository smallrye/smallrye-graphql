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

Example of server code
```java
@GraphQLApi
public class RoleApi {
    @Query
    public List<Role> findAllRolesByUserId(@NonNull UUID userId) {
        // return roles
    }

    public List<Permission> permission(@Source Roles role, @DefaultValue("5") int limit) {
        // return permissions, based on roles
    }

    public List<PermissionType> permissionType(@Source Permission permission, @DefaultValue("5") int limit) {
        // return permissionType, based on permission
    }
}
```

Query looks like
```
query {
  findAllRolesByUserId(userId: ...) {
    id
    permission(limit: 2) {
      id
      permissionType(limit: 3) {
        id
      }
    }
  }
}
```

On client side you can create next code
```java
public record PermissionType(Long id) {
}

public record Permission(Long id, List<PermissionType> permissionType) {
}

public record Role(UUID id, List<Permission> permission) {
}

@GraphQLClientApi
public interface ApiClient {
    List<Role> findAllRolesByUserId(
            UUID userId,
            @NestedParameter("permission") @Name("limit") int permissionLimit,
            @NestedParameter("permission.permissionType") @Name("limit") int permissionTypeLimit
    );
}
```

Namespaces
==========
> [NOTE]
> We strongly unrecommended the use namespaces with a type-safe client. 
> It is possible to use the @Name annotation, with minimal changes to the code. 
> However, for more complex cases it is better to use the dynamic client, since understanding the resulting code can be difficult.

There are several ways to work with namespaces with the type-safe client.

If only 1 level of nesting is used, then the interface can be marked with the @Name annotation.

```
query {
    users {
        findAll {
         ....
        }
    }
}
```

```java
@Name("users")
@GraphQLClientApi
public interface ApiClient {
    List<User> findAll();
}
```

You also can use Wrapper class (but the code doesn't look great.).

Modify server code from above.
```java
@Name("roles")  // Add namespace
@GraphQLApi
public class RoleApi {
    ////
}
```

Client code
```java
// Name field roles as method named
public record RolesWrapper(List<RolesTest> findAllRolesByUserId) {
}

@GraphQLClientApi
public interface ApiClient {
    @Query("roles") // required naming as namespace
    RolesWrapper getRoles( // extend nested params
            @NestedParameter("findAllRolesByUserId") UUID userId, // here roles id name of field in wrapper class
            @NestedParameter("findAllRolesByUserId.permission") @Name("limit") int permissionLimit,
            @NestedParameter("findAllRolesByUserId.permission.permissionType") @Name("limit") int permissionTypeLimit
    );
}
```

If you have more than 1 level of nesting and want to use a type-safe client, the only way is with wrapper classes.
```java
@Name("admin")
@GraphQLApi
public class RoleApi {
    public static class RoleQueryNamespace{ }

    @Query("roles")
    public RoleQueryNamespace roleQueryNamespace(){
        return new RoleQueryNamespace();
    }

    public List<Role> findAll(@Source RoleQueryNamespace namespace, UUID userId) {}
    public List<Permission> permission(@Source Role role, @DefaultValue("5") int limit) {}
    public List<PermissionType> permissionType(@Source Permission permissions, @DefaultValue("5") int limit) {}
}
```

Query will be like
```
query {
  admin {
    roles {
      findAll(userId: ...) {
        id
        permission(limit: 1) {
          id
          permissionType(limit: 2) {
            id
          }
        }
      }
    }
  }
}
```

```java
// Without Name annotation it must have name as is in schema
public record AdminWrapper(RolesWrapper roles) { // namespace in schema
    public record RolesWrapper(List<RolesTest> findAllRolesByUserId){} // name method in schema
}

@GraphQLClientApi(endpoint = "http://localhost:8081/graphql")
public interface ApiClient {
    @Query("admin")
    AdminWrapper findAllRolesByUserId(
            // nested params must be such as field names in wrapper class
            @NestedParameter("roles.findAllRolesByUserId") UUID userId,
            @NestedParameter("roles.findAllRolesByUserId.permission") @Name("limit") int permissionLimit,
            @NestedParameter("roles.findAllRolesByUserId.permission.permissionType") @Name("limit") int permissionTypeLimit
    );
}
/// or
public record AdminWrapper(@Name("roles") RolesWrapper rolesWrapper) { // @Name like namespace in schema
    public record RolesWrapper(@Name("findAllRolesByUserId") List<Role> roles){} // @Name like method name in schema
}

@GraphQLClientApi
public interface ApiClient {
    @Query("admin")
    AdminWrapper findAllRolesByUserId(
            // nested params value must be such as field names in wrapper class
            @NestedParameter("rolesWrapper.roles") UUID userId,
            @NestedParameter("rolesWrapper.roles.permission") @Name("limit") int permissionLimit,
            @NestedParameter("rolesWrapper.roles.permission.permissionType") @Name("limit") int permissionTypeLimit
    );
}
```
As you can see, the code with wrapper classes is quite messy, so we don't recommend using this approach in a type-safe client.
