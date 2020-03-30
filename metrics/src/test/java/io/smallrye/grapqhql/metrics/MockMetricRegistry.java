/*
 * Copyright 2020 IBM Corporation and others.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 * http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package io.smallrye.graphql.metrics;

import java.util.HashMap;
import java.util.Map;
import java.util.SortedMap;
import java.util.SortedSet;

import org.eclipse.microprofile.metrics.*;

public class MockMetricRegistry extends MetricRegistry {

    Map<String, SimpleTimer> simpleTimers = new HashMap<>();

    @Override
    public SimpleTimer simpleTimer(String name) {
        SimpleTimer simpleTimer = simpleTimers.computeIfAbsent(name, k -> new MockSimpleTimer());
        return simpleTimer;
    }

    @Override
    public ConcurrentGauge concurrentGauge(Metadata metadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConcurrentGauge concurrentGauge(Metadata metadata, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConcurrentGauge concurrentGauge(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public ConcurrentGauge concurrentGauge(String name, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Counter counter(Metadata metadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Counter counter(Metadata metadata, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Counter counter(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Counter counter(String name, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, ConcurrentGauge> getConcurrentGauges() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, ConcurrentGauge> getConcurrentGauges(MetricFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, Counter> getCounters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, Counter> getCounters(MetricFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, Gauge> getGauges() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, Gauge> getGauges(MetricFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, Histogram> getHistograms() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, Histogram> getHistograms(MetricFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<String, Metadata> getMetadata() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, Meter> getMeters() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, Meter> getMeters(MetricFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<MetricID> getMetricIDs() {
        throw new UnsupportedOperationException();
    }

    @Override
    public Map<MetricID, Metric> getMetrics() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedSet<String> getNames() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, SimpleTimer> getSimpleTimers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, SimpleTimer> getSimpleTimers(MetricFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, Timer> getTimers() {
        throw new UnsupportedOperationException();
    }

    @Override
    public SortedMap<MetricID, Timer> getTimers(MetricFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Histogram histogram(Metadata metadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Histogram histogram(Metadata metadata, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Histogram histogram(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Histogram histogram(String name, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Meter meter(Metadata metadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Meter meter(Metadata metadata, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Meter meter(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Meter meter(String name, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Metric> T register(Metadata metadata, T metric) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Metric> T register(Metadata metadata, T metric, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public <T extends Metric> T register(String name, T metric) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(MetricID metricID) {
        throw new UnsupportedOperationException();
    }

    @Override
    public boolean remove(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public void removeMatching(MetricFilter filter) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleTimer simpleTimer(Metadata metadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleTimer simpleTimer(Metadata metadata, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public SimpleTimer simpleTimer(String name, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer timer(Metadata metadata) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer timer(Metadata metadata, Tag... tags) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer timer(String name) {
        throw new UnsupportedOperationException();
    }

    @Override
    public Timer timer(String name, Tag... tags) {
        throw new UnsupportedOperationException();
    }
}