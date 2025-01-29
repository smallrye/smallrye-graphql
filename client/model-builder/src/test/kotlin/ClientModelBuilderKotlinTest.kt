package io.smallrye.graphql.client.model

import io.smallrye.graphql.client.typesafe.api.GraphQLClientApi
import org.eclipse.microprofile.graphql.Query
import org.jboss.jandex.Index
import org.junit.jupiter.api.Assertions.assertEquals
import org.junit.jupiter.api.Assertions.assertNotNull
import org.junit.jupiter.api.Test

class ClientModelBuilderKotlinTest {
    @GraphQLClientApi(configKey = "some-client")
    interface ClientApi {
        @Query
        fun returnNonNullFloat(someFloat: Float): Float

        @Query
        fun returnNullableString(someString: String?): String?

        @Query
        fun returnList(list: List<String>): String

        // fixme: nullability is bit inconsistent from kotlin

    }

    @Test
    fun basicClientModelTest() {
        val configKey = "some-client"
        val clientModels = ClientModelBuilder.build(Index.of(ClientApi::class.java))
        assertNotNull(clientModels)
        val clientModel = clientModels.getClientModelByConfigKey(configKey)
        assertNotNull(clientModel)
        assertEquals(3, clientModel.operationMap.size)
        assertOperation(clientModel, MethodKey("returnNonNullFloat", arrayOf(Float::class.java)), "query returnNonNullFloat(\$someFloat: Float!) { returnNonNullFloat(someFloat: \$someFloat) }")
        assertOperation(clientModel, MethodKey("returnNullableString", arrayOf(String::class.java)), "query returnNullableString(\$someString: String) { returnNullableString(someString: \$someString) }")
        assertOperation(clientModel, MethodKey("returnList", arrayOf(List::class.java)), "query returnList(\$list: [String!]!) { returnList(list: \$list) }")
    }

    private fun assertOperation(clientModel: ClientModel, methodKey: MethodKey, expectedQuery: String) {
        val actualQuery = clientModel.operationMap[methodKey]
        assertEquals(expectedQuery, actualQuery)
    }
}