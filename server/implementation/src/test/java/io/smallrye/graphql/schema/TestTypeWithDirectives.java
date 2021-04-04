package io.smallrye.graphql.schema;

@IntArrayTestDirective({ 1, 2, 3 })
public class TestTypeWithDirectives {
    private String value;

    public String getValue() {
        return value;
    }

    public void setValue(String value) {
        this.value = value;
    }
}
