package io.smallrye.graphql.jackson.jsonb;

import com.fasterxml.jackson.databind.module.SimpleModule;

public class JsonbCompatModule extends SimpleModule {

    private static final long serialVersionUID = 1L;

    public JsonbCompatModule() {
        super("JsonbCompatModule");
    }

    @Override
    public void setupModule(SetupContext context) {
        super.setupModule(context);
        context.insertAnnotationIntrospector(new JsonbAnnotationIntrospector());
    }
}
