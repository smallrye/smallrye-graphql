package io.smallrye.graphql.test.apps.mapping.api;

import java.util.Currency;

import javax.json.bind.adapter.JsonbAdapter;

/**
 * Map a Currency to and from json
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CurrencyAdapter implements JsonbAdapter<Currency, String> {

    @Override
    public String adaptToJson(Currency currency) {
        return currency.getCurrencyCode();
    }

    @Override
    public Currency adaptFromJson(String currencyCode) {
        return Currency.getInstance(currencyCode);
    }
}