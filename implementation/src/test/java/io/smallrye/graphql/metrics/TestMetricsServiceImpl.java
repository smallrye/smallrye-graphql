package io.smallrye.graphql.metrics;

import java.time.Duration;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;
import java.util.concurrent.Callable;

import org.eclipse.microprofile.metrics.ConcurrentGauge;
import org.eclipse.microprofile.metrics.Counter;
import org.eclipse.microprofile.metrics.Gauge;
import org.eclipse.microprofile.metrics.Histogram;
import org.eclipse.microprofile.metrics.Metadata;
import org.eclipse.microprofile.metrics.Meter;
import org.eclipse.microprofile.metrics.Metric;
import org.eclipse.microprofile.metrics.MetricFilter;
import org.eclipse.microprofile.metrics.MetricID;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricRegistry.Type;
import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.Tag;
import org.eclipse.microprofile.metrics.Timer;

import io.smallrye.graphql.spi.MetricsService;

public class TestMetricsServiceImpl implements MetricsService {

    static TestMetricsServiceImpl INSTANCE;
    final MockMetricsRegistry vendorRegistry = new MockMetricsRegistry();

    @Override
    public String getName() {
        return "Test MetricsServiceImpl";
    }

    @Override
    public MetricRegistry getMetricRegistry(Type type) {
        if (INSTANCE == null) {
            INSTANCE = this;
        }
        if (type.equals(Type.VENDOR)) {
            return vendorRegistry;
        }
        return null;
    }

    public static class MockMetricsRegistry extends MetricRegistry {

        final List<Metadata> simpleTimeMetadatas = new ArrayList<>();
        final Map<String, SimpleTimer> simpleTimers = new HashMap<>();

        @Override
        public <T extends Metric> T register(String name, T metric) throws IllegalArgumentException {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public <T extends Metric> T register(Metadata metadata, T metric) throws IllegalArgumentException {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public <T extends Metric> T register(Metadata metadata, T metric, Tag... tags) throws IllegalArgumentException {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Counter counter(String name) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Counter counter(String name, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Counter counter(Metadata metadata) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Counter counter(Metadata metadata, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public ConcurrentGauge concurrentGauge(String name) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public ConcurrentGauge concurrentGauge(String name, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public ConcurrentGauge concurrentGauge(Metadata metadata) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public ConcurrentGauge concurrentGauge(Metadata metadata, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Histogram histogram(String name) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Histogram histogram(String name, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Histogram histogram(Metadata metadata) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Histogram histogram(Metadata metadata, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Meter meter(String name) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Meter meter(String name, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Meter meter(Metadata metadata) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Meter meter(Metadata metadata, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Timer timer(String name) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Timer timer(String name, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Timer timer(Metadata metadata) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Timer timer(Metadata metadata, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SimpleTimer simpleTimer(String name) {
            return simpleTimers.computeIfAbsent(name, k -> new MockSimpleTimer());
        }

        @Override
        public SimpleTimer simpleTimer(String name, Tag... tags) {
            return simpleTimers.computeIfAbsent(name, k -> new MockSimpleTimer());
        }

        @Override
        public SimpleTimer simpleTimer(Metadata metadata) {
            simpleTimeMetadatas.add(metadata);
            return simpleTimers.computeIfAbsent(metadata.getName(), k -> new MockSimpleTimer());
        }

        @Override
        public SimpleTimer simpleTimer(Metadata metadata, Tag... tags) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public boolean remove(String name) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public boolean remove(MetricID metricID) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public void removeMatching(MetricFilter filter) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedSet<String> getNames() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedSet<MetricID> getMetricIDs() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, Gauge> getGauges() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, Gauge> getGauges(MetricFilter filter) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, Counter> getCounters() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, Counter> getCounters(MetricFilter filter) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, ConcurrentGauge> getConcurrentGauges() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, ConcurrentGauge> getConcurrentGauges(MetricFilter filter) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, Histogram> getHistograms() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, Histogram> getHistograms(MetricFilter filter) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, Meter> getMeters() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, Meter> getMeters(MetricFilter filter) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, Timer> getTimers() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, Timer> getTimers(MetricFilter filter) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, SimpleTimer> getSimpleTimers() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public SortedMap<MetricID, SimpleTimer> getSimpleTimers(MetricFilter filter) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Map<MetricID, Metric> getMetrics() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Map<String, Metadata> getMetadata() {
            throw new UnsupportedOperationException("unimplemented");
        }
    }

    static class MockSimpleTimer implements SimpleTimer {

        Duration totalDuration = Duration.ZERO;
        long count = 0;

        @Override
        public synchronized void update(Duration duration) {
            totalDuration = totalDuration.plus(duration);
            count++;
        }

        @Override
        public <T> T time(Callable<T> event) throws Exception {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public void time(Runnable event) {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Context time() {
            throw new UnsupportedOperationException("unimplemented");
        }

        @Override
        public Duration getElapsedTime() {
            return totalDuration;
        }

        @Override
        public long getCount() {
            return count;
        }
    }
}
