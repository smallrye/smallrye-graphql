package io.smallrye.graphql.scalar.federation;

import io.smallrye.graphql.api.federation.link.Import;
import io.smallrye.graphql.scalar.AbstractScalar;

/**
 * Scalar for {@link Import}.
 */
public class ImportScalar extends AbstractScalar {

    public ImportScalar() {
        super("Import", new ImportCoercing(), Import.class);
    }
}
