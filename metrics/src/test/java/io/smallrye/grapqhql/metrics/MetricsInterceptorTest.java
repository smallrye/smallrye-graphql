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

import static org.junit.Assert.assertEquals;

import java.lang.reflect.Method;

import org.junit.Test;

public class MetricsInterceptorTest {

    @Test
    public void testMetricsInterceptor() throws Exception {
        Method mockGraphQLApiMethod = MetricsInterceptorTest.class.getMethod("testMetricsInterceptor");
        MockMetricRegistry mockRegistry = new MockMetricRegistry();
        MetricsInterceptor interceptor = new MockMetricsInterceptor(mockRegistry);
        MockInvocationContext mockInvocationContext = new MockInvocationContext();
        mockInvocationContext.method = mockGraphQLApiMethod;
        mockInvocationContext.proceedSupplier = () -> {
            return "It worked!";
        };

        String metricName = "mp_graphql_io.smallrye.graphql.metrics.MetricsInterceptorTest.testMetricsInterceptor";
        Object o = interceptor.captureMetrics(mockInvocationContext);
        assertEquals("It worked!", o);
        assertEquals(1, mockRegistry.simpleTimers.size());
        assertEquals(1, mockRegistry.simpleTimers.get(metricName).getCount());

        o = interceptor.captureMetrics(mockInvocationContext);
        assertEquals("It worked!", o);
        assertEquals(1, mockRegistry.simpleTimers.size());
        assertEquals(2, mockRegistry.simpleTimers.get(metricName).getCount());

    }
}
