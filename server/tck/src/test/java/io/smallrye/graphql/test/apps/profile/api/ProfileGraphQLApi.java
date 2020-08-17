package io.smallrye.graphql.test.apps.profile.api;

import java.util.ArrayList;
import java.util.List;

import javax.inject.Inject;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.smallrye.graphql.api.Context;

/**
 * Profile API
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@GraphQLApi
public class ProfileGraphQLApi {

    int sourceMethodExecutionCount = 0;

    @Inject
    Context context;

    @Query("profile")
    @Description("Get a Profile by ID")
    public Profile getProfile(int profileId) {
        return ProfileDB.getProfile(profileId);
    }

    @Query
    public List<Profile> getProfiles() {
        return ProfileDB.getProfiles();
    }

    @Query("configurationByName")
    @Description("Get a configuration by name")
    public Configuration getByName(@Name("name") ConfigurationEnum name) {
        return Configuration.getByName(name.toString());
    }

    @Query("context")
    public String getPathFromContext() {
        return context.getPath();
    }

    @Mutation
    @Description("Add a new Profile")
    public Profile addProfile(Profile profile) {
        return ProfileDB.addProfile(profile);
    }

    // This method will be ignored due to below batch
    public Integer getCount(@Source Profile profile) {
        sourceMethodExecutionCount = sourceMethodExecutionCount + 1;

        return sourceMethodExecutionCount;
    }

    public List<Integer> getCount(@Source List<Profile> profiles) {
        sourceMethodExecutionCount = sourceMethodExecutionCount + 1;
        List<Integer> batched = new ArrayList<>();
        for (Profile profile : profiles) {
            batched.add(sourceMethodExecutionCount);
        }
        return batched;
    }

    public List<Timestamp> getTimestamp(@Source List<Profile> profiles) {
        List<Timestamp> batched = new ArrayList<>();
        for (Profile profile : profiles) {
            batched.add(new Timestamp()); // Test that transformation still works on batch
        }
        return batched;
    }
}
