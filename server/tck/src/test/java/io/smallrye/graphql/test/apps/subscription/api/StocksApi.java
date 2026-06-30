package io.smallrye.graphql.test.apps.subscription.api;

import java.util.List;
import java.util.concurrent.Flow;

import org.eclipse.microprofile.graphql.Description;
import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.eclipse.microprofile.graphql.Subscription;
import org.reactivestreams.Publisher;

import io.smallrye.mutiny.Multi;
import mutiny.zero.flow.adapters.AdaptersToFlow;

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
        return Multi.createFrom().publisher(AdaptersToFlow.publisher(getStockQuotePublisher(stockCode)));
    }

    @Subscription("listSubscription")
    @Description("Test a List subscription")
    public Multi<List<String>> listSubscription() {
        return Multi.createFrom().empty();
    }

    @Subscription("arraySubscription")
    @Description("Test an Array subscription")
    public Multi<String[]> arraySubscription() {
        return Multi.createFrom().empty();
    }

    @Subscription
    @Description("Get stock quote changes using Flow.Publisher (MicroProfile spec requirement)")
    public Flow.Publisher<Stock> getStockQuoteFlow(String stockCode) {
        return AdaptersToFlow.publisher(STOCK_TICKER_PUBLISHER.getPublisher());
    }

    // TODO: Support other Publisher types ?

}
