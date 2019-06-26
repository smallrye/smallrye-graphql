package io.leangen.graphql.metadata.strategy.query;

import java.lang.reflect.Member;
import java.lang.reflect.Method;
import java.util.Collection;
import java.util.function.Predicate;

import io.leangen.graphql.metadata.Resolver;

public interface ResolverBuilder {

    Predicate<Member> ACCEPT_ALL = member -> true;
    Predicate<Member> REAL_ONLY = member -> (!(member instanceof Method) || !((Method) member).isBridge())
            && !member.isSynthetic();

    Collection<Resolver> buildQueryResolvers(ResolverBuilderParams params);

    Collection<Resolver> buildMutationResolvers(ResolverBuilderParams params);

    Collection<Resolver> buildSubscriptionResolvers(ResolverBuilderParams params);
}
