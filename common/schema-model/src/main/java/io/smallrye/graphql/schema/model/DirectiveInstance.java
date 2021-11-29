package io.smallrye.graphql.schema.model;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A built-in or {@link DirectiveType custom} directive applied to some location.
 *
 * @see <a href="https://spec.graphql.org/draft/#Directive">Directive</a>
 */
public class DirectiveInstance {
    private DirectiveType type;
    private Map<String, Object> values = new LinkedHashMap<>();

    public DirectiveType getType() {
        return type;
    }

    public void setType(DirectiveType type) {
        this.type = type;
    }

    public Object getValue(String name) {
        return values.get(name);
    }

    public void setValue(String name, Object value) {
        values.put(name, value);
    }

    public Map<String, Object> getValues() {
        return values;
    }

    public void setValues(Map<String, Object> values) {
        this.values = values;
    }

    @Override
    public String toString() {
        return "DirectiveInstance{" + "type=" + type + ", values=" + values + '}';
    }
}
