package io.smallrye.graphql.client.core;

import static io.smallrye.graphql.client.core.utils.ServiceUtils.getNewInstanceOf;

public interface FragmentReference extends FieldOrFragment {

    static FragmentReference fragmentRef(String name) {
        FragmentReference ref = getNewInstanceOf(FragmentReference.class);
        ref.setName(name);
        return ref;
    }

    static FragmentReference fragmentRef(Fragment fragment) {
        FragmentReference ref = getNewInstanceOf(FragmentReference.class);
        ref.setName(fragment.getName());
        return ref;
    }

    String getName();

    void setName(String name);

}
