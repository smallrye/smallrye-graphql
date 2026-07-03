# Target fields for input types

The `@Source` annotation extends **output** types by adding externally-defined fields. Its
counterpart for **input** types is `@Target` (`io.smallrye.graphql.api.Target`).

With `@Target`, you can add fields to a GraphQL input type the same way `@Source` adds fields to
an output type. The annotated parameter receives the input object, and a sibling parameter
receives the value sent by the client for that field. The method name becomes the input field
name in the schema (overridable with `@Name`), and `@Description` can be added on the method.

Server-side example:

```java
import io.smallrye.graphql.api.Target;

@GraphQLApi
public class CharacterService {

    @Mutation
    public Character save(Character input) {
        // the input may have been modified by @Target methods below
        return input;
    }

    public void applyTwitterHandle(@Target Character character, String twitterHandle) {
        character.setTwitterHandle(twitterHandle);
    }
}
```

This generates an extended input type in the schema:

```graphql
input CharacterInput {
    # ... regular fields ...
    twitterHandle: String   # added by the applyTwitterHandle method
}
```

When a client sends a mutation that includes the `twitterHandle` field, the corresponding
`@Target` method is invoked at runtime. If the field is omitted, the method is not called.

Multiple `@Target` methods on the same input type are supported:

```java
public void applyTwitterHandle(@Target Character character, String twitterHandle) {
    character.setTwitterHandle(twitterHandle);
}

public void applyNumber(@Target Character character, Integer rating) {
    character.setRating(rating);
}
```

A `Context` parameter can be injected into target methods:

```java
public void applyWithContext(@Target Character character, String value, Context context) {
    String fieldName = context.getFieldName();
    // ...
}
```

> [NOTE]
> `@Target` is currently marked as experimental.
