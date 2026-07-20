package io.smallrye.graphql.execution;

import org.jboss.logging.Logger;

import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.SerializationFeature;
import tools.jackson.databind.json.JsonMapper;
import tools.jackson.databind.node.JsonNodeFactory;
import tools.jackson.databind.node.ObjectNode;

/**
 * A default implementation for Execution Response Writer
 *
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class JsonObjectResponseWriter implements ExecutionResponseWriter {
    protected static final Logger LOG = Logger.getLogger(JsonObjectResponseWriter.class.getName());

    private static final ObjectMapper PRETTY_MAPPER = JsonMapper.builder()
            .enable(SerializationFeature.INDENT_OUTPUT)
            .build();
    private static final JsonNodeFactory NODE_FACTORY = JsonNodeFactory.instance;

    private ExecutionResponse executionResponse = null;
    private Throwable throwable = null;
    private final ObjectNode input;

    JsonObjectResponseWriter(String graphQL) {
        this.input = NODE_FACTORY.objectNode().put("query", graphQL);
    }

    JsonObjectResponseWriter(ObjectNode input) {
        this.input = input;
    }

    @Override
    public void write(ExecutionResponse er) {
        this.executionResponse = er;
    }

    @Override
    public void fail(Throwable t) {
        this.throwable = t;
    }

    public void logInput() {
        LOG.info(getPrettyJson(input));
    }

    public void logOutput() {
        if (executionResponse != null) {
            LOG.info(getPrettyJson(executionResponse.getExecutionResultAsJsonObject()));
        } else if (throwable != null) {
            LOG.error("ERROR", throwable);
        }
    }

    public ObjectNode getInput() {
        return this.input;
    }

    public ObjectNode getOutput() {
        if (this.executionResponse != null) {
            return this.executionResponse.getExecutionResultAsJsonObject();
        }
        return NODE_FACTORY.objectNode();
    }

    public ExecutionResponse getExecutionResponse() {
        return this.executionResponse;
    }

    private String getPrettyJson(ObjectNode node) {
        try {
            return PRETTY_MAPPER.writeValueAsString(node);
        } catch (JacksonException e) {
            throw new RuntimeException(e);
        }
    }
}
