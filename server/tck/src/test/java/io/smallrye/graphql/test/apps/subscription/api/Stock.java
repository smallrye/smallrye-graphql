package io.smallrye.graphql.test.apps.subscription.api;

import java.math.BigDecimal;
import java.time.LocalDateTime;

import org.eclipse.microprofile.graphql.DateFormat;
import org.eclipse.microprofile.graphql.NonNull;

/**
 * Representing a stock
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class Stock {
    @NonNull
    @DateFormat("dd MMMM yyyy 'at' HH:mm:ss")
    public LocalDateTime dateTime;
    @NonNull
    public String stockCode;
    public BigDecimal stockPrice;
    public BigDecimal stockPriceChange;

    public Stock() {

    }

    public Stock(LocalDateTime dateTime, String stockCode, BigDecimal stockPrice, BigDecimal stockPriceChange) {
        this.dateTime = dateTime;
        this.stockCode = stockCode;
        this.stockPrice = stockPrice;
        this.stockPriceChange = stockPriceChange;
    }

    @Override
    public String toString() {
        return "Stock{" + "dateTime=" + dateTime + ", stockCode=" + stockCode + ", stockPrice=" + stockPrice
                + ", stockPriceChange=" + stockPriceChange + '}';
    }
}
