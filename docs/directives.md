# Directives

## Custom Directives

You can add your own [GraphQL Directives](https://spec.graphql.org/draft/#sec-Language.Directives) by writing
a corresponding Java Annotation and annotate it as `@Directive`, e.g.:

```java
@Directive(on = { OBJECT, INTERFACE })
@Description("Just a test")
@Retention(RUNTIME)
public @interface MyDirective {
}
```

Directives can be repeatable, see the `@Key` annotation for an example.

## Directives generated from Bean Validation annotations

If your project uses Bean Validation to validate fields on input types and operation arguments, and you enable 
inclusion of directives in the schema (by setting `smallrye.graphql.schema.includeDirectives=true`), 
then constraints decoded from annotations will be added to your schema as directives. This is currently only 
supported for some built-in constraints (annotations from the 
`javax.validation.constraints` package), and custom constraints aren't supported at all. 

Each bean validation annotation is mapped to a single `@constraint` directive. The directive is declared as repeatable,
so if you have multiple constraints on an input field, the field will contain multiple `@constraint` directives.
The following table describes the mapping between BV annotations and `@constraint` directives (all currently supported 
BV annotations are listed here): 

| BV annotation | GraphQL directive |
| ------------ | ------------- |
| `@Size(MIN, MAX)` | `@constraint(minLength=MIN, maxLength=MAX)` |
| `@Email` | `@constraint(format='email')` |
| `@Max` | `@constraint(max=VALUE)` |
| `@Min` | `@constraint(min=VALUE)` |
| `@Pattern(REGEXP)` | `@constraint(pattern=REGEXP)` |

Note: The `@NotNull` annotation does not map to a directive, instead it makes the GraphQL type non-nullable.

Constraints will only appear on fields of input types and operation arguments.
