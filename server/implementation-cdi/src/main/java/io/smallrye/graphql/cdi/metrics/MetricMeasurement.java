package io.smallrye.graphql.cdi.metrics;

public class MetricMeasurement {
    private String name;
    private boolean source;
    private String operationType;
    private long timeStarted;

    public MetricMeasurement(String name, boolean source, String operationType, long timeStarted) {
        this.name = name;
        this.source = source;
        this.operationType = operationType;
        this.timeStarted = timeStarted;
    }

    public String getName() {
        return name;
    }

    public boolean isSource() {
        return source;
    }

    public String getOperationType() {
        return operationType;
    }

    public long getTimeStarted() {
        return timeStarted;
    }
}
