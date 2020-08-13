package io.smallrye.graphql.cdi.validation;

import java.lang.reflect.Method;
import java.util.Set;

import javax.validation.ConstraintViolation;
import javax.validation.Validation;
import javax.validation.ValidatorFactory;

import io.smallrye.graphql.cdi.config.ConfigKey;
import io.smallrye.graphql.execution.event.InvokeInfo;
import io.smallrye.graphql.spi.EventingService;

/**
 * Validate input before execution
 */
public class ValidationService implements EventingService {
    private static final ValidatorFactory VALIDATOR_FACTORY = Validation.buildDefaultValidatorFactory();

    @Override
    public void beforeInvoke(InvokeInfo invokeInfo) throws Exception {
        System.out.println(">>>>>>>>>>>> BEFORE INVOKE");
        Object declaringObject = invokeInfo.getOperationInstance();
        System.out.println(">>>>>>>>>>>> declaringObject = " + declaringObject);
        Method method = invokeInfo.getOperationMethod();
        System.out.println(">>>>>>>>>>>> method = " + method);

        Object[] arguments = invokeInfo.getOperationTransformedArguments();

        System.out.println(">>>>>>>>>>>> arguments = " + arguments);
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
}
