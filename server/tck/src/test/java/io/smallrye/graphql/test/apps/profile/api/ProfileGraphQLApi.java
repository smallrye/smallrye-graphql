package io.smallrye.graphql.test.apps.profile.api;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Name;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

/**
 * Profile API
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@GraphQLApi
public class ProfileGraphQLApi {

    int sourceMethodExecutionCount = 0;

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

    @Query
    public List<List<List<Profile>>> deepList() {
        List<Profile> dl1 = new ArrayList<>();
        List<List<Profile>> dl2 = new ArrayList<>();
        List<List<List<Profile>>> dl3 = new ArrayList<>();

        dl1.add(ProfileDB.getProfile(1));
        dl2.add(dl1);
        dl3.add(dl2);
        return dl3;

    }

    @Query
    public List<List<List<Foo>>> deepListFoo() {
        List<Foo> dl1 = new ArrayList<>();
        List<List<Foo>> dl2 = new ArrayList<>();
        List<List<List<Foo>>> dl3 = new ArrayList<>();

        dl1.add(new Foo("bar"));
        dl2.add(dl1);
        dl3.add(dl2);
        return dl3;

    }

    // This method will be ignored due to below batch
    public Timestamp getTimestamp(@Source Profile profile, int random) {
        return new Timestamp();
    }

    public List<Timestamp> getTimestamp(@Source List<Profile> profiles, int random) {
        List<Timestamp> batched = new ArrayList<>();
        for (Profile profile : profiles) {
            batched.add(new Timestamp()); // Test that transformation still works on batch
        }
        return batched;
    }
}
