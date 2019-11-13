/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */

package io.smallrye.graphql.execution;

import javax.enterprise.context.ApplicationScoped;
import javax.enterprise.inject.Produces;

import graphql.schema.GraphQLCodeRegistry;

/**
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
@ApplicationScoped
public class CodeRegistry {

    private GraphQLCodeRegistry.Builder codeRegistryBuilder = GraphQLCodeRegistry.newCodeRegistry();

    @Produces
    public GraphQLCodeRegistry.Builder getGraphQLCodeRegistryBuilder() {
        return this.codeRegistryBuilder;
    }
}
