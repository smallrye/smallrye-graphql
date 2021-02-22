package io.smallrye.graphql.test.apps.mapping.api;

import java.util.Currency;

import javax.json.Json;
import javax.json.JsonObject;
import javax.json.bind.adapter.JsonbAdapter;

/**
 * Map a Currency to and from json
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CurrencyAdapter implements JsonbAdapter<Currency, JsonObject> {

    @Override
    public JsonObject adaptToJson(Currency currency) {
        return Json.createObjectBuilder()
                .add("currencyCode", currency.getCurrencyCode())
                .build();
    }

    @Override
    public Currency adaptFromJson(JsonObject json) {
        return Currency.getInstance(json.getString("currencyCode"));
    }
}