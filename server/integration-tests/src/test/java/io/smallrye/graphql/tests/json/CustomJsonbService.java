package io.smallrye.graphql.tests.json;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

import io.smallrye.graphql.spi.EventingService;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;

public class CustomJsonbService implements EventingService {

    @Override
    public String getConfigKey() {
        return null; // activate always
    }

    @Override
    public Map<String, ObjectMapper> overrideObjectMapperConfig() {
        ObjectMapper mapper = JsonMapper.builder()
                .defaultDateFormat(new SimpleDateFormat("MM dd yyyy HH:mm Z"))
                .build();
        return Collections.singletonMap(DateWrapper.class.getName(), mapper);
    }
}
