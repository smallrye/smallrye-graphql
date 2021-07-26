package io.smallrye.graphql.cdi.config;

import org.eclipse.microprofile.config.spi.Converter;

import io.smallrye.graphql.spi.config.LogPayloadOption;

/**
 * Allow for using true/false as valid value (backward compatible)
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class LogPayloadOptionConverter implements Converter<LogPayloadOption> {

    @Override
    public LogPayloadOption convert(String configuredValue) {
        if (configuredValue != null && !configuredValue.isEmpty()) {
            if (configuredValue.equalsIgnoreCase("true")) {
                return LogPayloadOption.queryOnly;
            } else if (configuredValue.equalsIgnoreCase("false")) {
                return LogPayloadOption.off;
            }

            return LogPayloadOption.valueOf(configuredValue);
        }
        // default
        return LogPayloadOption.off;
    }

}
