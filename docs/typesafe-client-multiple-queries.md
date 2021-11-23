Multiple
========

Say you need the result from several root queries, e.g. all
`superHeroes` and all `superVillains`. Java only supports a single
return value, so you’ll need a wrapper class:

``` java
@GraphQLClientApi
interface SuperHeroesApi {
    HeroesAndVillains heroesAndVillains();
}

@Multiple
class HeroesAndVillains {
    List<SuperHero> superHeroes;
    List<SuperVillain> superVillains;
}
```

The `@Multiple` annotation 'inlines' the wrapper class, i.e. the actual
query is:

``` graphql
query heroesAndVillains { superHeroes {...} superVillains {...}}
```

The actual response below will be mapped to an instance of the
`HeroesAndVillains` wrapper class:

``` json
{
  "data": {
    "superHeroes": [ ... ],
    "superVillains": [ ... ]
  }
}
```

If the nested queries require parameters, use `@` annotations to put
them on the field (remember: GraphQL fields can have parameters).

If you need the same request several times (e.g. with different query
parameters), use `@Name` annotations, so the actual field names are used
as [alias](#_name_mapping_aliases).