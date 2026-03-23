# Extensions in SmallRye GraphQL

`SmallRyeContext` provides two separate extension mechanisms:

- **Outbound extensions** (`addedExtensions`) — extensions added by the server and sent back to the client in the response.
- **Inbound extensions** (`extensionsFromClient`) — extensions received from the client as part of the incoming request.

## Adding extensions to the response

**SmallRye GraphQL** allows the user to add their own extensions inside the GraphQL response
which is received from the server. The `extensions` are stored next to the `data` field in the GraphQL response.

``` json
{
  "data": {
    "shirt": {
      "size": "L"
    }
  },
  "extensions": {
    "country": "Germany",
    "pi": 3.1415926535
  }
}
```

To add extensions, you need to `@Inject` an instance of `SmallRyeContext` in your `@GraphQLApi` annotated class.
After that, you can add your own extensions via the method `addExtension`
with its input parameters: `key : String` as an identification of the added extension,
and `value: Object` as a value of the extension. Value can be any given object that can be converted into JsonValue.

As an example, this class below contains the query `getShirt` and during the http request, the query adds these extensions:
`{"country": "Germany", "pi": 3.1415926535}`.
These extensions will be sent back via response in the `extensions` field.
```java
import jakarta.inject.Inject;

@GraphQLApi
public class ShirtResources {
    @Inject
    SmallRyeContext smallRyeContext;

    @Query
    public Shirt getShirt() {
        smallRyeContext.addExtension("country", "Germany");
        smallRyeContext.addExtension("pi", 3.1415926535);
        //...
    }
}
```
> [NOTE]
> You can also use `getAddedExtensions()` to access the underlying map directly.

## Reading extensions from the client request

Clients can send an `extensions` map alongside the query (as defined by the GraphQL over HTTP specification).
On the server side, these can be read via `SmallRyeContext.getExtensionsFromClient()`, which returns a
`Map<String, Object>` (or `null` if the client did not send any extensions).

```java
import jakarta.inject.Inject;

@GraphQLApi
public class MyApi {
    @Inject
    SmallRyeContext smallRyeContext;

    @Query
    public String process() {
        Map<String, Object> clientExtensions = smallRyeContext.getExtensionsFromClient();
        if (clientExtensions != null) {
            String traceId = (String) clientExtensions.get("traceId");
            // ...
        }
        //...
    }
}
```