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

import java.lang.reflect.Method;

import javax.annotation.Priority;
import javax.enterprise.context.Dependent;
import javax.inject.Inject;
import javax.interceptor.AroundInvoke;
import javax.interceptor.Interceptor;
import javax.interceptor.InvocationContext;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.metrics.MetricRegistry;
import org.eclipse.microprofile.metrics.MetricRegistry.Type;
import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.SimpleTimer.Context;
import org.eclipse.microprofile.metrics.annotation.RegistryType;
import org.jboss.logging.Logger;

@Dependent
@GraphQLApi
@Interceptor
@Priority(Interceptor.Priority.PLATFORM_AFTER)
public class MetricsInterceptor {
    private final static Logger LOG = Logger.getLogger(MetricsInterceptor.class);
    private final static String PREFIX = "mp_graphql_";

    private MetricRegistry metrics;

    @AroundInvoke
    public Object captureMetrics(InvocationContext ctx) throws Exception {
        Method m = ctx.getMethod();
        String fqMethodName = PREFIX + m.getDeclaringClass().getName() + "." + m.getName();
        if (LOG.isDebugEnabled()) {
            LOG.debug("invoking " + fqMethodName);
        }
        SimpleTimer timer = getMetrics().simpleTimer(fqMethodName);
        try (Context c = timer.time()) {
            return ctx.proceed();
        } finally {
            if (LOG.isDebugEnabled()) {
                LOG.debug("invoked " + timer.getCount() + " times");
            }
        }
    }

    protected MetricRegistry getMetrics() {
        return metrics;
    }

    @Inject
    @RegistryType(type = Type.VENDOR)
    protected void setMetrics(MetricRegistry metrics) {
        this.metrics = metrics;
    }
}
