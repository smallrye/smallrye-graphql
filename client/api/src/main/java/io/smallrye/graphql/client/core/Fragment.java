package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceOf;
import static java.util.Arrays.asList;

import java.util.List;

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

    List<Field> getFields();

    void setFields(List<Field> fields);

    class FragmentBuilder {

        private String name;

        private String targetType;

        private List<Field> fields;

        FragmentBuilder(String name) {
            this.name = name;
        }

        public Fragment on(String targetType, Field... fields) {
            this.targetType = targetType;
            this.fields = asList(fields);
            return build();
        }

        Fragment build() {
            Fragment fragment = getNewInstanceOf(Fragment.class);
            fragment.setName(name);
            fragment.setTargetType(targetType);
            fragment.setFields(fields);
            return fragment;
        }
    }
}
