package io.smallrye.graphql.test.apps.adapt.with.api;

import java.util.ArrayList;
import java.util.List;

import io.smallrye.graphql.api.Adapter;

/**
 * Using an adapter to String
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class TagsAdapter implements Adapter<Tags, String> {

    @Override
    public Tags from(String tagsString) {
        String[] values = tagsString.split(" ");
        List<Tag> tagList = new ArrayList<>();
        for (String value : values) {
            tagList.add(new Tag(value));
        }
        Tags tags = new Tags();
        tags.taglist = tagList.toArray(new Tag[] {});
        return tags;
    }

    @Override
    public String to(Tags tags) {
        List<String> values = new ArrayList<>();
        for (Tag tag : tags.taglist) {
            values.add(tag.value);
        }
        return String.join(" ", values.toArray(new String[] {}));
    }
}
