# Typesafe Client Static Directives

## Custom Static Directives

The declaration of custom static directives for the typesafe client follows the same principles as the server-side [directives](directives.md). With only exception of using the [Executable Directive Locations](https://spec.graphql.org/draft/#ExecutableDirectiveLocation) instead of Type System Directive Locations.
```java
@Directive(on = { FIELD, VARIABLE_DEFINITION })
@Retention(RUNTIME)
public @interface MyDirective {
    String value() default "";
}
```

Applying this to your API should look like this:
```java
import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi;

@GraphQLClientApi
public @interface MyClientApi {
    
    @Query
    @MyDirective("top-level field")
    MySuperHero getMySuperHero(@MyDirective("variable definition") String nameOfMySuperHero);
    
    class MySuperHero {
        @MyDirective("field")
        String name;
    }
}
```
Your API is going to generate a query something like this:
```
query mySuperHero(
  $nameOfMySuperHero: String @myDirective(value: "variable definition")
) {
  mySuperHero(nameOfMySuperHero: $nameOfMySuperHero)
    @myDirective(value: "top-level field") {
    name @myDirective(value: "field")
  }
}
```

> [NOTE]
> The current implementation supports only FIELD and VARIABLE_DEFINITION locations.