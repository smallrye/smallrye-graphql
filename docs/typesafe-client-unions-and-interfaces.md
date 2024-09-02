Unions & Interfaces
===================
      
If you consume a GraphQL service with a schema containing either a `Union` or an `Interface`,
the client needs to request different fields via fragments, depending on the requested sub-type,
and deserialize a polymorphic type.

This is quite complex on the GraphQL side, but the typesafe client makes it very easy to use.

Unions
======

One common use-case for Unions is returning a business error, e.g. when searching for a super hero,
either you find a super hero and maybe you want the name and other fields,
or you don't find it, and maybe you want a message with the reason.
Such a query could look like this:

```graphql
query find($name: String) {
    find(name: $name){
        __typename
        ... on SuperHero {name}
        ... on SuperHeroNotFound {message}
    }
}
```

When it finds your super hero, the service could respond with this:

```json
{
  "data": {
    "find": {
      "__typename": "SuperHero",
      "name": "Spider-Man"
    }
  }
}
```

Or if it doesn't find it, the response could look like this:

```json
{
  "data": {
    "find": {
      "__typename":"SuperHeroNotFound",
      "message":"There is no hero named Foo"
    }
  }
}
```

On the Java side, this maps quite nicely to an interface, e.g. `SuperHeroResponse`
implemented by the two possible classes returned, e.g. `SuperHero` and `SuperHeroNotFound`;
you only need to provide the typesafe client with this information about these sub-classes by using the
[`JSON-B` Polymorphic Types](https://jakarta.ee/specifications/jsonb/3.0/jakarta-jsonb-spec-3.0#polymorphic-types)
annotations `@JsonbTypeInfo` and `@JsonbSubtype`:

```java
    @Union
    @JsonbTypeInfo(key = "__typename", value = {
            @JsonbSubtype(alias = "SuperHero", type = SuperHero.class),
            @JsonbSubtype(alias = "NotFound", type = NotFound.class)
    })
    public interface SuperHeroResponse {
    }

    public static class SuperHero implements SuperHeroResponse {
        String name;

        String getName() {
            return name;
        }
    }

    @Type("SuperHeroNotFound")
    public static class NotFound implements SuperHeroResponse {
        String message;

        String getMessage() {
            return message;
        }
    }

    @GraphQLClientApi
    interface UnionApi {
        SuperHeroResponse find(String name);
    }
```

Note that we rename the `NotFound` class into the GraphQL type `SuperHeroNotFound` to demo that use-case.

The `@Union` annotation is actually not used and only helps to document what's happening behind the scenes.

Interfaces
==========

If a service returns an GraphQL `interface`, that's visible only in the schema;
queries and responses work exactly like for a `union`.
So, interfaces are very similar to unions; but they can share common fields between the sub-types.

E.g. a query returning either a `MainCharacter` or a `SideKick`, could look like this: 

```graphql
query find($name: String) {
    find(name: $name){
        __typename
        ... on MainCharacter {name superPower}
        ... on SideKick {name mainCharacter}
    }
}
```

The Java side looks very similar the Union example above:

```java
@JsonbTypeInfo(key = "__typename", value = {
    @JsonbSubtype(alias = "MainCharacter", type = MainCharacter.class),
    @JsonbSubtype(alias = "SideKick", type = SideKick.class)
})
public interface SearchResult {
    String getName();
}

public static class MainCharacter implements SearchResult {
    String name;
    String superPower;

    @Override
    public String getName() {
        return name;
    }

    public String getSuperPower() {
        return superPower;
    }
}

public static class SideKick implements SearchResult {
    String name;
    String mainCharacter;

    @Override
    public String getName() {
        return name;
    }

    public String getMainCharacter() {
        return mainCharacter;
    }
}

@GraphQLClientApi
interface InterfaceApi {
    SearchResult find(String name);

    List<SearchResult> all();
}
```
