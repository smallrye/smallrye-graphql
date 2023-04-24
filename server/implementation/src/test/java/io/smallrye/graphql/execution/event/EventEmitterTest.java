package io.smallrye.graphql.execution.event;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.spi.EventingService;

class EventEmitterTest {

    public static int counter = 0;

    @BeforeEach
    void reset() {
        counter = 0;
        TestEventingService.reset();
        FirstEventingService.reset();
        LastEventingService.reset();
    }

    @Test
    void testOrderingOfEventingServices() throws NoSuchFieldException, IllegalAccessException {
        // When all instances are declared in the same service file they are loaded in the order declared.
        EventEmitter instance = EventEmitter.getInstance();
        Field enabledServicesField = EventEmitter.class.getDeclaredField("enabledServices");
        enabledServicesField.setAccessible(true);
        List<EventingService> enabledServices = (List<EventingService>) enabledServicesField.get(instance);
        assertThat(enabledServices.stream().map(s -> s.getClass()).collect(Collectors.toList()))
                .isEqualTo(asList(FirstEventingService.class, TestEventingService.class, LastEventingService.class));
    }

    @Test
    void testInvocationOrderBeforeExecute() {
        EventEmitter instance = EventEmitter.getInstance();
        instance.fireBeforeExecute(mock(Context.class));
        assertThat(
                asList(TestEventingService.invocationOrder, FirstEventingService.invocationOrder,
                        LastEventingService.invocationOrder))
                .isEqualTo(asList(1, 0, 2));
    }

    @Test
    void testInvocationOrderAfterExecute() {
        EventEmitter instance = EventEmitter.getInstance();
        instance.fireAfterExecute(mock(Context.class));
        assertThat(
                asList(TestEventingService.invocationOrder, FirstEventingService.invocationOrder,
                        LastEventingService.invocationOrder))
                .isEqualTo(asList(1, 2, 0));
    }
}
