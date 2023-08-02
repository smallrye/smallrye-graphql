package io.smallrye.graphql.schema.rolesallowedschemas;

import jakarta.annotation.security.RolesAllowed;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Mutation;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Source;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;
import io.smallrye.mutiny.operators.multi.processors.BroadcastProcessor;

@GraphQLApi
@RolesAllowed(value = "employee")
public class RolesSchema2 {

    @Query
    public int getMoney() {
        return 100;
    }

    @RolesAllowed(value = "admin")
    @Query
    public int getAdminMoney() {
        return 101;
    }

    public String getPassword(@Source Customer customer) {
        return "admin admin";
    }

    @RolesAllowed(value = "admin")
    public String getAdminPassword(@Source Customer customer) {
        return "123456";
    }

    @Mutation
    public Customer createCustomer(Customer customer) {
        return customer;
    }

    @Subscription
    public Multi<Customer> customerCreated() {
        return BroadcastProcessor.create();
    }

}
