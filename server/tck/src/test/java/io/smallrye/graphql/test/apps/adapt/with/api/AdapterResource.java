package io.smallrye.graphql.test.apps.adapt.with.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

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

        d.wordNumber = new WordNumber("seven");

        // Map that use User supplied adapter
        Map<String, String> metadata = new HashMap<>();
        metadata.put("foo", "bar");
        metadata.put("description", "here some more info");
        d.metadata = metadata;

        // Map that use auto adapting (basic scalar)
        Map<String, String> properties = new HashMap<>();
        properties.put("foo2", "bar2");
        properties.put("description2", "here some more info2");
        d.properties = properties;

        // Map that use auto adapting (complex objects)
        Map<ComplexKey, ComplexValue> complexMap = new HashMap<>();
        complexMap.put(new ComplexKey(1, "k1", "The complex key's key 1"), new ComplexValue(1, "v1a", "v1b"));
        complexMap.put(new ComplexKey(2, "k2", "The complex key's key 2"), new ComplexValue(1, "v2a", "v2b"));
        d.complexMap = complexMap;

        return d;
    }

    @Query
    public List<AdapterData> getAdapterDatas() {
        List<AdapterData> adapterDatas = new ArrayList<>();
        adapterDatas.add(getAdapterData());
        return adapterDatas;
    }

    @Mutation
    public AdapterData updateAdapterData(AdapterData adapterData) {
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

        @AdaptWith(WordNumberAdapter.class)
        public WordNumber wordNumber;

        @AdaptWith(CustomMapAdapter.class)
        public Map<String, String> metadata;

        public Map<String, String> properties;

        public Map<ComplexKey, ComplexValue> complexMap;

        @Override
        public String toString() {
            return "AdapterData{" + "id=" + id + ", name=" + name + ", email=" + email + ", address=" + address + '}';
        }
    }

}
