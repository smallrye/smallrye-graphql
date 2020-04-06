package io.smallrye.graphql.bootstrap.datafetcher;

import org.jboss.logging.Logger;

import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.cdi.CDIDelegate;

/**
 * Fetch data using CDI and Reflection
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public class CdiReflectionDataFetcher implements DataFetcher {
    private static final Logger LOG = Logger.getLogger(CdiReflectionDataFetcher.class.getName());

    private final CDIDelegate cdiDelegate = CDIDelegate.delegate();

    private final String declaringClass;

    public CdiReflectionDataFetcher(String declaringClass) {
        this.declaringClass = declaringClass;
    }

    @Override
    public Object get(DataFetchingEnvironment dfe) throws Exception {

        Object cdiBeanInstance = cdiDelegate.getInstanceFromCDI(declaringClass);
        LOG.error("We should be making a call on this bean : " + cdiBeanInstance.getClass().getName());
        return null;
    }

}
