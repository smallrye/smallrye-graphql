package io.smallrye.graphql.test.apps.subscription.api;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.reactivestreams.Publisher;

import io.smallrye.graphql.api.Subscription;
import io.smallrye.mutiny.Multi;

@GraphQLApi
public class StocksApi {

    private final static StockTickerPublisher STOCK_TICKER_PUBLISHER = new StockTickerPublisher();

    @Query
    public String[] getStockCodes() {
        return new String[] { "TEAM", "IBM", "AMZN", "MSFT", "GOOGL" };
    }

    @Subscription
    @Description("Get stock quote changes as they happen using Reactive Streams")
    public Publisher<Stock> getStockQuotePublisher(String stockCode) {
        return STOCK_TICKER_PUBLISHER.getPublisher();
    }

    @Subscription
    @Description("Get stock quote changes as they happen using Mutiny")
    public Multi<Stock> getStockQuoteMulti(String stockCode) {
        return Multi.createFrom().publisher(getStockQuotePublisher(stockCode));
    }

    // TODO: Support other Publisher types ?

}