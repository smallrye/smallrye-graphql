package io.smallrye.graphql.test.apps.profile.api;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

/**
 * Person API
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@GraphQLApi
public class PersonGraphQLApi {

    @Query("person")
    @Description("Get a Person by ID")

    public Person getPerson(int personId) {
        return PersonDB.getPerson(personId);
    }
}
