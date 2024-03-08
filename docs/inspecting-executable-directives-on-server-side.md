# Inspecting executable directives on the server side

Let's say you have this _GraphQL query_ with an executable directive on a field _location_.
```
{
    hero(name: $nameValue) {
        name
        location @customDirective(arg: true)
    }
}
```

To inspect your directive on the server side, you must inject a `SmallRyeContext` object and get the relevant information like this.
```java
    @Inject
    SmallRyeContext context;
    
    @Query
    public SuperHero getHero(String name) {
        // ...
        SelectionSet selectionSet = (SelectionSet) context.getDataFetchingEnvironment().getField().getChildren().get(0);
        Optional<Node> locationNode = selectionSet.getChildren().stream()
            .filter(node -> node instanceof Field && ((Field) node).getName().equals("location")).findAny(); // "location" is the field name where the directive is applied.
        locationNode.ifPresent(node -> {
            List<Directive> directives = ((Field) node).getDirectives();
            System.out.println(directives);
            });
        // the rest of the query...
    }
```
> [NOTE] 
> that `Field` refers to `graphql.language.Field`, not `io.smallrye.graphql.schema.model.Field`
