package io.smallrye.graphql.tests.client.dynamic;

import org.eclipse.microprofile.graphql.Name;

public class Dummy {

    private String string;

    private Integer integer;

    @Name("specialName")
    private String renamedField;

    public String getString() {
        return string;
    }

    public void setString(String string) {
        this.string = string;
    }

    public Integer getInteger() {
        return integer;
    }

    public void setInteger(Integer integer) {
        this.integer = integer;
    }

    public String getRenamedField() {
        return renamedField;
    }

    public void setRenamedField(String renamedField) {
        this.renamedField = renamedField;
    }
}
