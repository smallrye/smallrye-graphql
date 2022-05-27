package io.smallrye.graphql.test.apps.result.api;

import static java.util.function.Function.identity;
import static java.util.stream.Collectors.toMap;

import java.time.LocalDate;
import java.util.List;
import java.util.Map;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;

import io.smallrye.graphql.api.GraphQLResult;

@GraphQLApi
public class ResultWrapperTestingApi {
    private static final List<Order> ORDER_LIST = List.of(
            new Order("1", LocalDate.of(2022, 5, 11)),
            new Order("2", LocalDate.of(2022, 5, 21)),
            new Order("3", LocalDate.of(2022, 5, 31)));
    private static final Map<String, Order> ORDERS = ORDER_LIST.stream().collect(toMap(Order::getId, identity()));

    @Query
    @GraphQLResult
    public Order order(String id) {
        if ("9".equals(id))
            throw new RuntimeException("we have a (technical) problem");
        var order = ORDERS.get(id);
        if (order == null)
            throw new OrderNotFoundException();
        return order;
    }
}
