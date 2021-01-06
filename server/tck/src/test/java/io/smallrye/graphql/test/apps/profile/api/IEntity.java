package io.smallrye.graphql.test.apps.profile.api;

import org.eclipse.microprofile.graphql.Description;

import io.smallrye.graphql.api.Scalar;
import io.smallrye.graphql.api.ToScalar;

public interface IEntity {

    @Description("Testing map scalar to scalar")
    @ToScalar(Scalar.Int.class)
    public Long getId();

}
