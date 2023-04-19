Returning Void Type in Mutations with SmallRye GraphQL
=======
While by default **MicroProfile GraphQL** specification doesn't provide direct support for returning `void` type from mutation operations, **SmallRye GraphQL** has implemented this feature based on user feedback.

To implement a **mutation** operation that returns `void` or `Void` type, you can use the following pattern in your annotated `@GraphQLApi` class:
```java
    @Mutation
    public void createHero(SuperHero hero) {
        superHeroService.create(hero);
    }
```
In this example, `createHero()` receives a SuperHero object as input,
performs some operation with it (in this case, creating it using a
`superHeroService`).

> [NOTE]
> On the GraphQL level, this is achieved by returning a special scalar type named Void from the mutation.
> This scalar type will always have a value of null.