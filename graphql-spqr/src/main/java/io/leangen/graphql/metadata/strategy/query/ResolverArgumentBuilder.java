package io.leangen.graphql.metadata.strategy.query;

import java.util.List;

import io.leangen.graphql.metadata.OperationArgument;

/**
 * Created by bojan.tomic on 7/17/16.
 */
public interface ResolverArgumentBuilder {

    List<OperationArgument> buildResolverArguments(ArgumentBuilderParams params);
}
