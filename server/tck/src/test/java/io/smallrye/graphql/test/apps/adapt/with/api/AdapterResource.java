package io.smallrye.graphql.test.apps.adapt.with.api;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.stream.Collectors;

import javax.json.bind.annotation.JsonbTypeAdapter;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.smallrye.graphql.api.AdaptWith;

@GraphQLApi
public class AdapterResource {

    @Query
    public Map<String, String> mapOperationBasic() {
        Map<String, String> m = createBasicMap();
        return m;
    }

    @Query
    public Map<ComplexKey, ComplexValue> mapOperationComplex() {
        Map<ComplexKey, ComplexValue> m = createComplexMap();
        return m;
    }

    @Query
    public Map<ISO6391, Language> mapOperationEnum(Optional<ISO6391> forLang) {
        Map<ISO6391, Language> m = getEnumMap();
        if (forLang.isPresent()) {
            return m.entrySet()
                    .stream().filter(x -> x.getKey().equals(forLang.get()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            return m;
        }
    }

    public Map<String, String> mapSourceBasic(@Source AdapterData adapterData) {
        Map<String, String> m = createBasicMap();
        return m;
    }

    public Map<ComplexKey, ComplexValue> mapSourceComplex(@Source AdapterData adapterData) {
        Map<ComplexKey, ComplexValue> m = createComplexMap();
        return m;
    }

    public Map<ISO6391, Language> mapSourceEnum(@Source AdapterData adapterData, Optional<ISO6391> forLang) {
        Map<ISO6391, Language> m = getEnumMap();
        if (forLang.isPresent()) {
            return m.entrySet()
                    .stream().filter(x -> x.getKey().equals(forLang.get()))
                    .collect(Collectors.toMap(Map.Entry::getKey, Map.Entry::getValue));
        } else {
            return m;
        }
    }

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
        Map<ComplexKey, ComplexValue> complexMap = createComplexMap();
        d.complexMap = complexMap;

        // Map that use auto adapting (enum keys)
        Map<ISO6391, Language> langMap = getEnumMap();
        d.langMap = langMap;

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

    private Map<String, String> createBasicMap() {
        Map<String, String> m = new HashMap<>();
        m.put("key1", "value1");
        m.put("key2", "value2");
        m.put("key3", "value3");
        return m;
    }

    private Map<ComplexKey, ComplexValue> createComplexMap() {
        Map<ComplexKey, ComplexValue> complexMap = new HashMap<>();
        complexMap.put(new ComplexKey(1, "k1", "The complex key's key 1"), new ComplexValue(1, "v1a", "v1b"));
        complexMap.put(new ComplexKey(2, "k2", "The complex key's key 2"), new ComplexValue(1, "v2a", "v2b"));
        return complexMap;
    }

    private Map<ISO6391, Language> getEnumMap() {
        Map<ISO6391, Language> langMap = new HashMap<>();
        langMap.put(ISO6391.en, new Language(ISO6391.en, "english", "english", "please", "thank you"));
        langMap.put(ISO6391.af, new Language(ISO6391.af, "afrikaans", "afrikaans", "asseblief", "dankie"));
        langMap.put(ISO6391.de, new Language(ISO6391.de, "deutsch", "german", "bitte", "danke dir"));
        langMap.put(ISO6391.fr, new Language(ISO6391.fr, "français", "french", "s'il te plaît", "merci"));
        return langMap;
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

        public Map<ISO6391, Language> langMap;

        @Override
        public String toString() {
            return "AdapterData{" + "id=" + id + ", name=" + name + ", email=" + email + ", address=" + address + '}';
        }
    }

}
