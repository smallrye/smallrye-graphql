package io.smallrye.graphql.tests.json;

import java.util.Collections;
import java.util.Map;

import jakarta.json.bind.Jsonb;
import jakarta.json.bind.JsonbBuilder;
import jakarta.json.bind.JsonbConfig;

import io.smallrye.graphql.spi.EventingService;

public class CustomJsonbService implements EventingService {

    @Override
    public String getConfigKey() {
        return null; // activate always
    }

    @Override
    public Map<String, Jsonb> overrideJsonbConfig() {
        JsonbConfig config = new JsonbConfig().withDateFormat("MM dd yyyy HH:mm Z", null);
        return Collections.singletonMap(DateWrapper.class.getName(), JsonbBuilder.create(config));
    }
}
