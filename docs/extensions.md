# Adding extensions to GraphQL response
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
After that, you can add your own extensions via the method `addExtensions`
with its input parameters: `key : String` as an identification of the added extension.
And `value: Object` as a value of the extension. Value can be any given object that can be converted into JsonValue.

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
        smallRyeContext.addExtensions("country", "Germany");
        smallRyeContext.addExtensions("pi", 3.1415926535);
        //...
    }
}
```
> [NOTE]
> You can also use the method `setAddedExtensions(Map<String, Object> addedExtensions)` to set all the extensions with map instance.
