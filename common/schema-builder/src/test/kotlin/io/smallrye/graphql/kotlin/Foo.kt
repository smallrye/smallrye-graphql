package io.smallrye.graphql.kotlin

import io.smallrye.mutiny.Uni
import org.eclipse.microprofile.graphql.GraphQLApi
import org.eclipse.microprofile.graphql.Query
import org.eclipse.microprofile.graphql.Source
import jakarta.json.bind.annotation.JsonbCreator

data class Foo @JsonbCreator constructor(val bar: String?)
data class Foo2 @JsonbCreator constructor(val bar: String?)
data class Foo3 @JsonbCreator constructor(val bar: String?)
data class Foo4 @JsonbCreator constructor(val bar: String?)

@GraphQLApi
class Example {
  @Query
  fun nullable(): Foo? = null

  @Query
  fun notNullable(): Foo = Foo("blabla")

  @Query
  fun nullableItemInUni(): Uni<Foo?> = Uni.createFrom().nullItem()

  @Query
  fun notNullableItemInUni(): Uni<Foo> = Uni.createFrom().item(Foo("blabla"))

  fun nullableNestedItem(@Source foo: Foo): String? = foo.bar

  fun notNullableNestedItem(@Source foo: Foo): String = "bar"

  fun nullableNestedItemInUni(@Source foo: Foo): Uni<String?> = Uni.createFrom().nullItem()

  fun notNullableNestedItemInUni(@Source foo: Foo): Uni<String> = Uni.createFrom().item("bar")

  // some overloaded methods to make sure we correctly find the function inside KotlinClassMetadata
  @Query("zzz1")
  fun zzz(x: Foo): Uni<Foo?> = Uni.createFrom().nullItem()

  @Query("zzz2")
  fun zzz(x: Foo2): Uni<Foo?> = Uni.createFrom().nullItem()

  @Query("zzz3")
  fun zzz(x: Foo3): Uni<Foo> = Uni.createFrom().nullItem()

  @Query("zzz4")
  fun zzz(x: Foo4): Uni<Foo> = Uni.createFrom().nullItem()

}