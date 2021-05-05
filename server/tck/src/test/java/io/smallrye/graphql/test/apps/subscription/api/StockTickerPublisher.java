package io.smallrye.graphql.test.apps.subscription.api;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

import org.reactivestreams.Publisher;

import io.reactivex.BackpressureStrategy;
import io.reactivex.Flowable;
import io.reactivex.Observable;
import io.reactivex.ObservableEmitter;
import io.reactivex.observables.ConnectableObservable;

public class StockTickerPublisher {

    private final Flowable<Stock> publisher;

    public StockTickerPublisher() {
        Observable<Stock> stockPriceUpdateObservable = Observable.create(emitter -> {

            ScheduledExecutorService executorService = Executors.newScheduledThreadPool(1);
            executorService.scheduleAtFixedRate(newStockTick(emitter), 0, 2, TimeUnit.SECONDS);

        });

        ConnectableObservable<Stock> connectableObservable = stockPriceUpdateObservable.share().publish();
        connectableObservable.connect();

        publisher = connectableObservable.toFlowable(BackpressureStrategy.BUFFER);
    }

    public Publisher<Stock> getPublisher() {
        return publisher;
    }

    public Publisher<Stock> getPublisher(List<String> stockCodes) {
        return publisher.filter(stock -> stockCodes.contains(stock.stockCode));
    }

    private Runnable newStockTick(ObservableEmitter<Stock> emitter) {
        return () -> {
            List<Stock> stockPriceUpdates = getUpdates(rollDice(0, 5));
            if (stockPriceUpdates != null) {
                emitStocks(emitter, stockPriceUpdates);
            }
        };
    }

    private void emitStocks(ObservableEmitter<Stock> emitter, List<Stock> stocks) {
        for (Stock stock : stocks) {
            try {
                emitter.onNext(stock);
            } catch (RuntimeException rte) {
                rte.printStackTrace();
            }
        }
    }

    private List<Stock> getUpdates(int number) {
        List<Stock> updates = new ArrayList<>();
        for (int i = 0; i < number; i++) {
            updates.add(rollUpdate());
        }
        return updates;
    }

    private final static Map<String, BigDecimal> CURRENT_STOCK_PRICES = new ConcurrentHashMap<>();

    static {
        CURRENT_STOCK_PRICES.put("TEAM", dollars(39, 64));
        CURRENT_STOCK_PRICES.put("IBM", dollars(147, 10));
        CURRENT_STOCK_PRICES.put("AMZN", dollars(1002, 94));
        CURRENT_STOCK_PRICES.put("MSFT", dollars(77, 49));
        CURRENT_STOCK_PRICES.put("GOOGL", dollars(1007, 87));
    }

    private Stock rollUpdate() {
        ArrayList<String> STOCK_CODES = new ArrayList<>(CURRENT_STOCK_PRICES.keySet());

        String stockCode = STOCK_CODES.get(rollDice(0, STOCK_CODES.size() - 1));
        BigDecimal currentPrice = CURRENT_STOCK_PRICES.get(stockCode);

        BigDecimal incrementDollars = dollars(rollDice(0, 1), rollDice(0, 99));
        if (rollDice(0, 10) > 7) {
            // 0.3 of the time go down
            incrementDollars = incrementDollars.negate();
        }
        BigDecimal newPrice = currentPrice.add(incrementDollars);

        CURRENT_STOCK_PRICES.put(stockCode, newPrice);
        return new Stock(LocalDateTime.now(), stockCode, newPrice, incrementDollars);
    }

    private static BigDecimal dollars(int dollars, int cents) {
        return truncate("" + dollars + "." + cents);
    }

    private static BigDecimal truncate(final String text) {
        BigDecimal bigDecimal = new BigDecimal(text);
        if (bigDecimal.scale() > 2)
            bigDecimal = new BigDecimal(text).setScale(2, RoundingMode.HALF_UP);
        return bigDecimal.stripTrailingZeros();
    }

    private final static Random rand = new Random();

    private static int rollDice(int min, int max) {
        return rand.nextInt((max - min) + 1) + min;
    }

}
