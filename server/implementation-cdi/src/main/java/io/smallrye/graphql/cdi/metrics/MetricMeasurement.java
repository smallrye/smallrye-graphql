package io.smallrye.graphql.cdi.metrics;

public class MetricMeasurement<M> {
    private String name;
    private boolean source;
    private String operationType;
    private M metric;

    public MetricMeasurement(String name, boolean source, String operationType, M metric) {
        this.name = name;
        this.source = source;
        this.operationType = operationType;
        this.metric = metric;
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

    public M getMetric() {
        return metric;
    }
}
