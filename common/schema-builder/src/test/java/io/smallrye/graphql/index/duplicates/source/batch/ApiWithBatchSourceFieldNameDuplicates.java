package io.smallrye.graphql.index.duplicates.source.batch;

import java.util.Collection;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.smallrye.graphql.index.duplicates.source.SomeClass;

@GraphQLApi
public class ApiWithBatchSourceFieldNameDuplicates {

    public Collection<String> getPassword(@Source Collection<SomeClass> someClasses) {
        return List.of("my new password 1", "my new password 2");
    }

    @Query
    public Collection<SomeClass> getSomeClass() {
        return List.of(new SomeClass("hello1", "password1"), new SomeClass("hello2", "password2"));
    }
}
