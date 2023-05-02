Handling of the WebSocket's init-payload
===========
When executing GraphQL operations over WebSockets (both with the `graphql-ws` and `graphql-transport-ws` protocols),
the client can send an initial payload
to the server when establishing the WebSocket connection. This payload can contain
any information that the server might need to process GraphQL requests, like client agent identification (similar to the `User-Agent` header) or data specific to the client. On the server-side, you can access this payload
via the `GraphQLContext`, which is available through the `DataFetchingEnvironment` object.
```java
@GraphQLApi
public class HeroResources {
    
    @Inject
    SmallryeContext context;

	// ... 
        Map<String, Object> initPayload = smallRyeContext
            .getDataFetchingEnvironment()
            .getGraphQlContext()
            .get("init-payload");
	// ...
}
```
