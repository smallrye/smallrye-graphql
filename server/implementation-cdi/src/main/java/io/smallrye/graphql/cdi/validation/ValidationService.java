package io.smallrye.graphql.cdi.validation;

import java.lang.reflect.Method;
import java.util.Set;

import jakarta.validation.ConstraintViolation;
import jakarta.validation.Validation;
import jakarta.validation.ValidatorFactory;

import io.smallrye.graphql.cdi.config.ConfigKey;
import io.smallrye.graphql.execution.event.InvokeInfo;
import io.smallrye.graphql.spi.EventingService;
import io.smallrye.graphql.spi.LookupService;

/**
 * Validate input before execution
 */
public class ValidationService implements EventingService {
    private static ValidatorFactory VALIDATOR_FACTORY = null;
    private final LookupService lookupService;

    public ValidationService() {
        this.lookupService = LookupService.get();
    }

    @Override
    public void beforeInvoke(InvokeInfo invokeInfo) throws Exception {
        Object declaringObject = invokeInfo.getOperationInstance();
        Method method = invokeInfo.getOperationMethod();

        Object[] arguments = invokeInfo.getOperationTransformedArguments();

        if (VALIDATOR_FACTORY == null) {
            VALIDATOR_FACTORY = getValidatorFactory();
        }
        Set<ConstraintViolation<Object>> violations = VALIDATOR_FACTORY.getValidator()
                .forExecutables().validateParameters(declaringObject, method, arguments);

        if (!violations.isEmpty()) {
            throw new BeanValidationException(violations, method);
        }
    }

    @Override
    public String getConfigKey() {
        return ConfigKey.ENABLE_VALIDATION;
    }

    private ValidatorFactory getValidatorFactory() {
        try {
            return lookupService.getInstance(ValidatorFactory.class).get();
        } catch (Exception t) {
            return Validation.buildDefaultValidatorFactory();
        }
    }
}
