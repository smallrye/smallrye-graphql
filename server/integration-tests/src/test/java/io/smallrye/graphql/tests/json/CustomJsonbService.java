package io.smallrye.graphql.tests.json;

import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Map;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.smallrye.graphql.spi.EventingService;

public class CustomJsonbService implements EventingService {

    @Override
    public String getConfigKey() {
        return null; // activate always
    }

    @Override
    public Map<String, ObjectMapper> overrideObjectMapperConfig() {
        ObjectMapper mapper = new ObjectMapper();
        mapper.setDateFormat(new SimpleDateFormat("MM dd yyyy HH:mm Z"));
        return Collections.singletonMap(DateWrapper.class.getName(), mapper);
    }
}
