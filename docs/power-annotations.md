Power Annotations
===========

Power-Annotations is actually independent of GraphQL. It’s a generic
mechanism for meta annotations. It consists of annotations you can put
on your code (these are meant to become a separate standard) and the
following implementations:

1.  A maven plugin to build a Jandex index file (and resolve all power
    annotations). This replaces the `jandex-maven-plugin`. It currently
    has fewer options, but as we only want to build what people actually
    need, give us a call if you need more!

2.  A classpath scanner to build a Jandex index at runtime (and resolve
    all power annotations).

There’s also a TCK to test, if an implementation adheres to the standard
defined by the annotations. It uses an Utils API, so the TCK can also
verify implementations not using Jandex. This Annotation Utils API may
be convenient for your usage as well, as, e.g., it also resolves
repeated annotations.

Annotations
===========

Stereotypes
-----------

The simplest use-case for Stereotypes is renaming annotations, e.g.
`@Property` instead of `@JsonbProperty` (assuming your JSON-B
implementation supported Power Annotations).

``` java
@Retention(RUNTIME)
@Stereotype
@JsonbProperty
public @interface Property {}
```

It gets much more interesting, when you add more annotations from other
(supporting) frameworks to your stereotype, e.g. `@XmlAttribute` from
JAX-B. Now you have one annotation instead of two with both having the
same semantics.

Properly used, stereotypes are shortcuts describing the role the
annotated element has; functionally as well as a documentation.

This is exactly what
[CDI-Stereotypes](https://jakarta.ee/specifications/cdi/2.0/cdi-spec-2.0.html#stereotypes)
do, but not restricted to CDI, i.e. the annotations used by any
framework that supports Power Annotations can be used with stereotypes.
We have our own `Stereotype` class; but as we don’t want to depend on
CDI and still not exclude CDI, any annotation named `Stereotype` will
work.

Mixins
------

Say you have a class you want to add annotations to, but you can’t;
e.g., because it’s a class from some library or even from the JDK. You
can create your own class (or interface or even annotation) and annotate
it as `@MixinFor` the target class. The annotations you put on your
mixin class will work as if they were on the target class (if your
framework supports Power Annotations).

This also works for annotations: say we’re developing an application
packed with annotations from JPA, which doesn’t support mixins (yet).
The application also uses a library that supports mixins but doesn’t
know about JPA, e.g. libraries like SmallRye GraphQL that use Jandex,
and you build the index with the `power-jandex-maven-plugin`. We want
all JPA `@Id` annotations to be recognized as synonyms for GraphQL `@Id`
annotations. We could create a simple mixin for the JPA annotation:

``` java
@MixinFor(jakarta.persistence.Id.class)
@org.eclipse.microprofile.graphql.Id
public class PersistenceIdMixin {}
```

Voilà! MP GraphQL work as if all JPA `@Id` annotations were its own.

> **Note**
>
> Mixins are a very powerful kind of magic: use them with caution and
> only when strictly necessary. Otherwise, the readers of your code will
> have a hard time to find out why something behaves as if an annotation
> was there, but it’s clearly not. If you can, use Stereotypes instead.
