package org.acme

@org.eclipse.microprofile.graphql.GraphQLApi
class TestingApi {

    @org.eclipse.microprofile.graphql.Query
    fun foo(): Foo? {
        return null
    }

}