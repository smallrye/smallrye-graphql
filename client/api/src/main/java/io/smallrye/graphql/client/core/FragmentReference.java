package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceFromFactory;
import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;

import java.util.List;

import io.smallrye.graphql.client.core.factory.FragmentReferenceFactory;

/**
 * Represents a reference to a named fragment.
 */
public interface FragmentReference extends FieldOrFragment {

    /**
     * Create a fragment reference by specifying the name of the target fragment.
     * In the resulting document, this will appear as `...FRAGMENTNAME`
     */
    static FragmentReference fragmentRef(String name) {
        FragmentReference ref = getNewInstanceFromFactory(FragmentReferenceFactory.class);
        ref.setName(name);
        ref.setDirectives(emptyList());
        return ref;
    }

    /**
     * Create a fragment reference by providing a built instance of a named fragment.
     * This will actually only use the name of the fragment - in the resulting document,
     * this will appear as `...FRAGMENTNAME`
     */
    static FragmentReference fragmentRef(Fragment fragment) {
        FragmentReference ref = getNewInstanceFromFactory(FragmentReferenceFactory.class);
        ref.setName(fragment.getName());
        ref.setDirectives(emptyList());
        return ref;
    }

    /**
     * Create a fragment reference by specifying the name of the target fragment and directives.
     * In the resulting document, this will appear as `...FRAGMENTNAME @DIRECTIVE`
     */
    static FragmentReference fragmentRefWithDirective(String name, Directive... directives) {
        FragmentReference ref = getNewInstanceFromFactory(FragmentReferenceFactory.class);
        ref.setName(name);
        ref.setDirectives(asList(directives));
        return ref;
    }

    /**
     * Create a fragment reference by providing a built instance of a named fragment and directives.
     * This will actually only use the name of the fragment - in the resulting document,
     * this will appear as `...FRAGMENTNAME @DIRECTIVE`
     */
    static FragmentReference fragmentRefWithDirective(Fragment fragment, Directive... directives) {
        FragmentReference ref = getNewInstanceFromFactory(FragmentReferenceFactory.class);
        ref.setName(fragment.getName());
        ref.setDirectives(asList(directives));
        return ref;
    }

    String getName();

    void setName(String name);

    List<Directive> getDirectives();

    void setDirectives(List<Directive> directives);
}
