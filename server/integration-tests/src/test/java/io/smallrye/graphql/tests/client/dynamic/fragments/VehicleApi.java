package io.smallrye.graphql.tests.client.dynamic.fragments;

import java.util.ArrayList;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

@GraphQLApi
public class VehicleApi {

    @Query
    public List<Vehicle> vehicles() {
        List<Vehicle> list = new ArrayList<>();
        list.add(new Car(8));
        list.add(new Bicycle(15));
        return list;
    }
}
