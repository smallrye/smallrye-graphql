# Custom scalar types

If the remote service contains custom scalar types represented as strings in GraphQL documents,
a typesafe client can learn to understand these types through introducing a class that represents them.
The class has to contain logic necessary for serializing and deserializing from/to raw strings.

A model class is considered to represent a scalar GraphQL type if it contains a static builder method
named `of`, `valueOf`, or `parse` whose sole parameter is a `java.lang.String` and it returns an 
instance of the class itself. The name of the GraphQL type is assumed to be equal to the short name
of the class.

For example, if the server supports a `URL` type (there is a `scalar URL` clause in the schema), 
create a class that represents a value for `URL`:

```java
public class URL {

    private String value;

    public URL(String value) {
        this.value = value;
    }

    public static URL valueOf(String value) {  // <1>
        return new URL(value);
    }

    @Override
    public String toString() {   // <2>
        return value;
    }
}
```

- <1> The static `valueOf(String)` method will be used for deserializing a `URL` from a string.
- <2> The `toString()` will be used for serializing a `URL` into a string to be sent to the GraphQL service