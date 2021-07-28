package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceOf;

/**
 * Represents a reference to a named fragment.
 */
public interface FragmentReference extends FieldOrFragment {

    /**
     * Create a fragment reference by specifying the name of the target fragment.
     * In the resulting document, this will appear as `...FRAGMENTNAME`
     */
    static FragmentReference fragmentRef(String name) {
        FragmentReference ref = getNewInstanceOf(FragmentReference.class);
        ref.setName(name);
        return ref;
    }

    /**
     * Create a fragment reference by providing a built instance of a named fragment.
     * This will actually only use the name of the fragment - in the resulting document,
     * this will appear as `...FRAGMENTNAME`
     */
    static FragmentReference fragmentRef(Fragment fragment) {
        FragmentReference ref = getNewInstanceOf(FragmentReference.class);
        ref.setName(fragment.getName());
        return ref;
    }

    String getName();

    void setName(String name);

}
