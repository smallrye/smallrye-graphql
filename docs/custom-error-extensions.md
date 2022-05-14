# GraphQL Error Extensions

Exceptions are reported in GraphQL in the `errors` array, next to the `data` field, so it's possible to return partial results, e.g.:

``` json
{
  "data": {
    "superHero": {
      "name": "Wolverine",
      "location": null
    }
  },
  "errors": [{
    "message":"location unknown",
    "path": ["superHero","location"],
    "extensions":{"code":"location-unknown"}
  }]
}
```

The `location` field couldn't be returned, and in the `errors`, there's the reason with a few predefined fields,
and a map of `extensions` that can contain custom details about the error.

You can add your own extensions by implementing the `io.smallrye.graphql.api.ErrorExtensionProvider` interface and
adding your class name to a file `META-INF/services/io.smallrye.graphql.api.ErrorExtensionProvider` (this is a `ServiceLoader`).

As an example, this class provides an extension named `exception-length` with the length of the simple class name of the exception:

```java
public class ExceptionLengthErrorExtensionProvider implements ErrorExtensionProvider {
    @Override
    public String getKey() {
        return "exception-length";
    }

    @Override
    public JsonNumber mapValueFrom(Throwable exception) {
        return Json.createValue(exception.getClass().getSimpleName().length());
    }
}
```
