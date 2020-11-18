package io.smallrye.graphql.client.typesafe.api;

import java.util.Objects;

public class SourceLocation {
    private final int line;
    private final int column;
    private final String sourceName;

    public SourceLocation() {
        this(0, 0, null);
    }

    public SourceLocation(int line, int column, String sourceName) {
        this.line = line;
        this.column = column;
        this.sourceName = sourceName;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o)
            return true;
        if (o == null || getClass() != o.getClass())
            return false;
        SourceLocation that = (SourceLocation) o;
        return line == that.line && column == that.column && Objects.equals(sourceName, that.sourceName);
    }

    @Override
    public int hashCode() {
        return Objects.hash(line, column, sourceName);
    }

    @Override
    public String toString() {
        return "(" + line + ":" + column + ((sourceName == null) ? "" : "@" + sourceName) + ")";
    }

    public int getLine() {
        return line;
    }

    public int getColumn() {
        return column;
    }

    public String getSourceName() {
        return sourceName;
    }
}
