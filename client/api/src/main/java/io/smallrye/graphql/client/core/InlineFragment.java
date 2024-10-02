package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

import io.smallrye.graphql.client.core.factory.InlineFragmentFactory;

/**
 * Represents an inline fragment in a GraphQL document. This can be used
 * anywhere where a field is expected (thus it implements `FieldOrFragment`).
 */
public interface InlineFragment extends FieldOrFragment {

    static InlineFragment on(String type, FieldOrFragment... fields) {
        InlineFragment fragment = getNewInstanceFromFactory(InlineFragmentFactory.class);

        fragment.setType(type);
        fragment.setDirectives(emptyList());
        fragment.setFields(asList(fields));

        return fragment;
    }

    static InlineFragment on(String type, List<Directive> directives, FieldOrFragment... fields) {
        InlineFragment fragment = getNewInstanceFromFactory(InlineFragmentFactory.class);

        fragment.setType(type);
        fragment.setDirectives(directives);
        fragment.setFields(asList(fields));

        return fragment;
    }

    static InlineFragment on(FieldOrFragment... fields) {
        return on("", fields);
    }

    static InlineFragment on(List<Directive> directives, FieldOrFragment... fields) {
        return on("", directives, fields);
    }

    String getType();

    void setType(String name);

    List<FieldOrFragment> getFields();

    void setFields(List<FieldOrFragment> fields);

    List<Directive> getDirectives();

    void setDirectives(List<Directive> directives);

}
