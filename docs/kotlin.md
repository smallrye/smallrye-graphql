# SmallRye Graphql in Kotlin

When working with SmallRye GraphQL in Kotlin, it is very important to use the `-Xemit-jvm-type-annotations` flag for the Kotlin compiler. This flag is important to generate the necessary annotations for reflection to work properly. The flag is supported as of Kotlin 1.3.70.