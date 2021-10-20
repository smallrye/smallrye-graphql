package io.smallrye.graphql.test.apps.adapter.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import javax.json.bind.annotation.JsonbTypeAdapter;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.AdaptWith;

@GraphQLApi
public class AdapterResource {

    @Query
    public AdapterData getAdapterData() {

        Tags tags = new Tags();
        List<Tag> tagList = new ArrayList<>();
        tagList.add(new Tag("foo"));
        tagList.add(new Tag("bar"));
        tags.taglist = tagList.toArray(new Tag[] {});

        AdapterData d = new AdapterData();
        d.id = 1L;
        d.name = "Foo";
        d.email = new EmailAddress("piet@pompies.co.za");
        d.tags = tags;
        Address address = new Address();
        address.lines = Arrays.asList(new String[] { "phillip@kruger.co.za" });
        address.addressType = AddressType.email;
        d.address = address;

        return d;
    }

    @Mutation
    public AdapterData updateAdapterData(AdapterData adapterData) {
        System.err.println("adapterData = " + adapterData);
        return adapterData;
    }

    public static class AdapterData {
        public Long id;

        public String name;

        @JsonbTypeAdapter(EmailAddressAdapter.class)
        public EmailAddress email;

        @AdaptWith(AddressAdapter.class)
        public Address address;

        @AdaptWith(TagsAdapter.class)
        public Tags tags;

        @Override
        public String toString() {
            return "AdapterData{" + "id=" + id + ", name=" + name + ", email=" + email + ", address=" + address + '}';
        }
    }

}
