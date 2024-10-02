package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;
import static io.smallrye.graphql.client.core.utils.validation.NameValidation.validateFragmentName;
import static io.smallrye.graphql.client.core.utils.validation.NameValidation.validateName;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

import io.smallrye.graphql.client.core.factory.FragmentFactory;

/**
 * Represents a named fragment definition in a GraphQL document. Such definition consists of a name,
 * target type, and a set of fields.
 */
public interface Fragment extends FragmentOrOperation {
    /*
     * Static factory methods
     */
    static List<Fragment> fragments(Fragment... fragments) {
        return asList(fragments);
    }

    static FragmentBuilder fragment(String name) {
        return new FragmentBuilder(name);
    }

    /*
     * Getter/Setter
     */
    String getName();

    void setName(String name);

    String getTargetType();

    void setTargetType(String name);

    List<FieldOrFragment> getFields();

    void setFields(List<FieldOrFragment> fields);

    List<Directive> getDirectives();

    void setDirectives(List<Directive> directives);

    class FragmentBuilder {

        private String name;

        private String targetType;

        private List<Directive> directives;

        private List<FieldOrFragment> fields;

        FragmentBuilder(String name) {
            this.name = validateFragmentName(name);
        }

        public Fragment on(String targetType, FieldOrFragment... fields) {
            this.targetType = validateName(targetType);
            this.directives = emptyList();
            this.fields = asList(fields);
            return build();
        }

        public Fragment on(String targetType, List<Directive> directives, FieldOrFragment... fields) {
            this.targetType = validateName(targetType);
            this.directives = directives;
            this.fields = asList(fields);
            return build();
        }

        Fragment build() {
            Fragment fragment = getNewInstanceFromFactory(FragmentFactory.class);
            fragment.setName(name);
            fragment.setTargetType(targetType);
            fragment.setDirectives(directives);
            fragment.setFields(fields);
            return fragment;
        }
    }
}
