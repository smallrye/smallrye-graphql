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

import java.time.Duration;
import java.util.concurrent.Callable;

import org.eclipse.microprofile.metrics.SimpleTimer;
import org.eclipse.microprofile.metrics.SimpleTimer.Context;

public class MockSimpleTimer implements SimpleTimer {

    private long count;
    private Duration elapsedTime = Duration.ofMillis(0);

    @Override
    public long getCount() {
        return count;
    }

    @Override
    public Duration getElapsedTime() {
        return elapsedTime;
    }

    @Override
    public Context time() {
        return new MockContext(this);
    }

    @Override
    public <T> T time(Callable<T> event) throws Exception {
        try (Context c = time()) {
            return event.call();
        }
    }

    @Override
    public void time(Runnable event) {
        try (Context c = time()) {
            event.run();
        }
    }

    @Override
    public void update(Duration duration) {
        elapsedTime.plus(duration);
    }

    public static class MockContext implements Context {
        final MockSimpleTimer simpleTimer;
        final long startTime;

        MockContext(MockSimpleTimer simpleTimer) {
            this.simpleTimer = simpleTimer;
            simpleTimer.count++;
            startTime = System.currentTimeMillis();
        }

        @Override
        public void close() {
            stop();
        }

        @Override
        public long stop() {
            long elapsed = System.currentTimeMillis() - startTime;
            simpleTimer.elapsedTime.plusMillis(elapsed);
            return elapsed;
        }
    }
}