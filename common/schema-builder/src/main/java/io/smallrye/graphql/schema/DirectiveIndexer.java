package io.smallrye.graphql.schema;

import java.io.IOException;

import org.jboss.jandex.IndexView;
import org.jboss.jandex.Indexer;

import io.smallrye.graphql.api.OneOf;
import io.smallrye.graphql.api.federation.Authenticated;
import io.smallrye.graphql.api.federation.ComposeDirective;
import io.smallrye.graphql.api.federation.Extends;
import io.smallrye.graphql.api.federation.External;
import io.smallrye.graphql.api.federation.FieldSet;
import io.smallrye.graphql.api.federation.Inaccessible;
import io.smallrye.graphql.api.federation.InterfaceObject;
import io.smallrye.graphql.api.federation.Key;
import io.smallrye.graphql.api.federation.Override;
import io.smallrye.graphql.api.federation.Provides;
import io.smallrye.graphql.api.federation.Requires;
import io.smallrye.graphql.api.federation.Shareable;
import io.smallrye.graphql.api.federation.Tag;
import io.smallrye.graphql.api.federation.link.Import;
import io.smallrye.graphql.api.federation.link.Link;
import io.smallrye.graphql.api.federation.link.Purpose;
import io.smallrye.graphql.api.federation.policy.Policy;
import io.smallrye.graphql.api.federation.policy.PolicyGroup;
import io.smallrye.graphql.api.federation.policy.PolicyItem;
import io.smallrye.graphql.api.federation.requiresscopes.RequiresScopes;
import io.smallrye.graphql.api.federation.requiresscopes.ScopeGroup;
import io.smallrye.graphql.api.federation.requiresscopes.ScopeItem;

class DirectiveIndexer {
    static IndexView directiveIndex() {
        Indexer indexer = new Indexer();

        try {
            // directives from the API module
            indexer.indexClass(io.smallrye.graphql.api.Deprecated.class);
            indexer.indexClass(java.lang.Deprecated.class);
            indexer.indexClass(OneOf.class);

            indexer.indexClass(Authenticated.class);
            indexer.indexClass(ComposeDirective.class);
            indexer.indexClass(Extends.class);
            indexer.indexClass(External.class);
            indexer.indexClass(FieldSet.class);
            indexer.indexClass(Inaccessible.class);
            indexer.indexClass(InterfaceObject.class);
            indexer.indexClass(Key.class);
            indexer.indexClass(Override.class);
            indexer.indexClass(Provides.class);
            indexer.indexClass(Requires.class);
            indexer.indexClass(Shareable.class);
            indexer.indexClass(Tag.class);

            indexer.indexClass(Link.class);
            indexer.indexClass(Import.class);
            indexer.indexClass(Purpose.class);

            indexer.indexClass(Policy.class);
            indexer.indexClass(PolicyGroup.class);
            indexer.indexClass(PolicyItem.class);

            indexer.indexClass(RequiresScopes.class);
            indexer.indexClass(ScopeGroup.class);
            indexer.indexClass(ScopeItem.class);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        return indexer.complete();
    }
}
