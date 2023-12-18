Creating GraphQL Custom Scalars with SmallRye GraphQL
=======
While by default **MicroProfile GraphQL** specification doesn't provide direct support for creating
custom scalars for GraphQL literal types of String, Int and Float, **SmallRye GraphQL** has implemented 
this feature based on user feedback. To implement a **custom scalar** with **SmallRye GraphQL**, 
you can annotate your custom scalar class with `@CustomScalar` in addition to following the pattern below:
```java
  @CustomScalar("BigDecimalString")
  public class BigDecimalString implements CustomStringScalar {
    public BigDecimalString(String stringValue) {
        ...
    }
    @Override
    public String stringValueForSerialization() {
        ...
  }
}
```
In this example, `BigDecimalString` implements the `CustomStringScalar` which is used to identify the 
proper (de) serialization for BigDecimalString.  `BigDecimalString` also provides a single argument
constructor which takes a String.  Finally, `BigDecimalString` implements 
`stringValueForSerialization()` which provides the String representation to be used during 
serialization.

> [NOTE]
> If the user wants to create a literal for GraphQL Int or Float, they would implement either 
> CustomIntScalar with the intValueForSerialization method or CustomFloatScalar with the
> floatValueForSerialization method respectively.