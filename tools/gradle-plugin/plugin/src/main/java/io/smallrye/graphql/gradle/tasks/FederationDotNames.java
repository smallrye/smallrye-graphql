package io.smallrye.graphql.gradle.tasks;

import io.smallrye.graphql.api.federation.Authenticated;
import io.smallrye.graphql.api.federation.ComposeDirective;
import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.Inaccessible;
import io.smallrye.graphql.api.federation.InterfaceObject;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.api.federation.Override;
import io.smallrye.graphql.api.federation.Provides;
import io.smallrye.graphql.api.federation.Requires;
import io.smallrye.graphql.api.federation.Shareable;
import io.smallrye.graphql.api.federation.Tag;
import io.smallrye.graphql.api.federation.link.Link;
import io.smallrye.graphql.api.federation.policy.Policy;
import io.smallrye.graphql.api.federation.requiresscopes.RequiresScopes;
import org.jboss.jandex.DotName;

import java.util.ArrayList;
import java.util.List;

public final class FederationDotNames {
    public static final List<DotName> FEDERATION_DIRECTIVES_NAMES;

    static {
        FEDERATION_DIRECTIVES_NAMES = new ArrayList<>();
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Authenticated.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(ComposeDirective.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Extends.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(External.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Inaccessible.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(InterfaceObject.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Key.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Override.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Provides.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Requires.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Shareable.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Tag.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Link.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(Policy.class));
        FEDERATION_DIRECTIVES_NAMES.add(DotName.createSimple(RequiresScopes.class));
    }
}
