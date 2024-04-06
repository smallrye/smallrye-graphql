package io.smallrye.graphql.index.duplicates.source.sourcefield;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.smallrye.graphql.index.duplicates.source.SomeClass;

@GraphQLApi
public class ApiWithSourceFieldNameDuplicates {

    public String getPassword(@Source SomeClass someClass) {
        return "new Password";
    }

    @Query
    public SomeClass getSomeClass() {
        return new SomeClass("hello", "password");
    }
}
