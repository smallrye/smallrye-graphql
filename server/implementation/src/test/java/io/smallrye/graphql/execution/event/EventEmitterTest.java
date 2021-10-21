package io.smallrye.graphql.execution.event;

import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.spi.EventingService;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;
import java.util.List;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;
import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

class EventEmitterTest {

    public static int counter = 0;

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
        TestEventingService.reset();
        FirstEventingService.reset();
        LastEventingService.reset();
        EventEmitter instance = EventEmitter.getInstance();
        instance.fireBeforeExecute(mock(Context.class));
        assertThat(
                asList(TestEventingService.invocationOrder,FirstEventingService.invocationOrder, LastEventingService.invocationOrder))
                .isEqualTo(asList(1, 0, 2));
    }

    @Test
    void testInvocationOrderAfterExecute() {
        TestEventingService.reset();
        FirstEventingService.reset();
        LastEventingService.reset();
        EventEmitter instance = EventEmitter.getInstance();
        instance.fireAfterExecute(mock(Context.class));
        assertThat(
                asList(TestEventingService.invocationOrder,FirstEventingService.invocationOrder, LastEventingService.invocationOrder))
                .isEqualTo(asList(1, 2, 0));
    }
}
