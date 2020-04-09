package io.smallrye.graphql.execution.datafetcher;

import java.lang.reflect.InvocationTargetException;
import java.text.NumberFormat;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAccessor;

import org.eclipse.microprofile.graphql.GraphQLException;
import org.jboss.logging.Logger;

import graphql.execution.DataFetcherExceptionHandlerParameters;
import graphql.execution.DataFetcherResult;
import graphql.execution.ExecutionPath;
import graphql.language.SourceLocation;
import graphql.schema.DataFetcher;
import graphql.schema.DataFetchingEnvironment;
import io.smallrye.graphql.bootstrap.TransformException;
import io.smallrye.graphql.bootstrap.datafetcher.ReflectionDataFetcherException;
import io.smallrye.graphql.execution.error.GraphQLExceptionWhileDataFetching;
import io.smallrye.graphql.execution.helper.FormatHelper;
import io.smallrye.graphql.schema.model.Field;
import io.smallrye.graphql.schema.model.Formatter;
import io.smallrye.graphql.schema.model.Method;

/**
 * a Datafetcher that can transform data (for now on the way out)
 * 
 * TODO: Consider some sort of decorator pattern ? Or java.lang.reflect.Proxy ?
 * TODO: Add Metrics back
 * 
 * @author Phillip Kruger (phillip.kruger@redhat.com)
 */
public abstract class AbstractTransformableDataFetcher implements DataFetcher {
    private static final Logger LOG = Logger.getLogger(AbstractTransformableDataFetcher.class.getName());

    private DateTimeFormatter dateTimeFormatter = null;
    private NumberFormat numberFormat = null;

    private final Method method;

    private boolean shouldTransformResponse = false;

    public AbstractTransformableDataFetcher(Method method) {
        this.method = method;

        if (method.getReturn().hasFormatter()) {
            if (method.getReturn().getFormatter().getType().equals(Formatter.Type.NUMBER)) {
                this.numberFormat = FormatHelper.getNumberFormat(method.getReturn().getFormatter());
                this.shouldTransformResponse = true;
            } else if (method.getReturn().getFormatter().getType().equals(Formatter.Type.DATE)) {
                this.dateTimeFormatter = FormatHelper.getDateFormat(method.getReturn().getFormatter());
                this.shouldTransformResponse = true;
            }
        }
    }

    @Override
    public Object get(DataFetchingEnvironment dfe) throws Exception {
        try {
            Object response = executeMethod(dfe);
            if (shouldTransformResponse) {
                return transform(response, method.getReturn());
            } else {
                return response;
            }
        } catch (TransformException pe) {
            return pe.getDataFetcherResult(dfe);
        } catch (InvocationTargetException | NoSuchMethodException | SecurityException | GraphQLException
                | IllegalAccessException | IllegalArgumentException ex) {
            Throwable throwable = ex.getCause();

            if (throwable == null) {
                throw new ReflectionDataFetcherException(ex);
            } else {
                if (throwable instanceof Error) {
                    throw (Error) throwable;
                } else if (throwable instanceof GraphQLException) {
                    GraphQLException graphQLException = (GraphQLException) throwable;
                    return getPartialResult(dfe, graphQLException);
                } else {
                    throw (Exception) throwable;
                }
            }
        }
    }

    private Object transform(Object o, Field field) {

        if (field.isCollection()) {
            LOG.warn("TODO: Transform [" + o.toString() + "] that is a [" + field.getJavaTypeClassName() + "]");
            // TODO: Call this until o.getClass equals field type
        }

        if (o.getClass().getName().equals(field.getTypeReference().getClassName())) {
            return transformObject(o);
        } else {
            // Throw an exception ?
            LOG.warn("Can not transform type [" + o.getClass().getName() + "] - expecting ["
                    + field.getTypeReference().getClassName() + "]");
            return o;
        }
    }

    private Object transformObject(Object o) {
        if (dateTimeFormatter != null) {
            return handleDateFormatting(o);
        } else if (numberFormat != null) {
            return handleNumberFormatting(o);
        } else {
            return o;
        }
    }

    private Object handleDateFormatting(Object o) {
        if (TemporalAccessor.class.isInstance(o)) {
            TemporalAccessor temporalAccessor = (TemporalAccessor) o;
            return dateTimeFormatter.format(temporalAccessor);
        } else {
            return o;
        }
    }

    private Object handleNumberFormatting(Object o) {
        if (Number.class.isInstance(o)) {
            Number number = (Number) o;
            return numberFormat.format(number);
        } else {
            return o;
        }
    }

    private DataFetcherResult<Object> getPartialResult(DataFetchingEnvironment dfe, GraphQLException graphQLException) {
        DataFetcherExceptionHandlerParameters handlerParameters = DataFetcherExceptionHandlerParameters
                .newExceptionParameters()
                .dataFetchingEnvironment(dfe)
                .exception(graphQLException)
                .build();

        SourceLocation sourceLocation = handlerParameters.getSourceLocation();
        ExecutionPath path = handlerParameters.getPath();
        GraphQLExceptionWhileDataFetching error = new GraphQLExceptionWhileDataFetching(path, graphQLException,
                sourceLocation);

        return DataFetcherResult.newResult()
                .data(graphQLException.getPartialResults())
                .error(error)
                .build();

    }

    protected abstract Object executeMethod(DataFetchingEnvironment dfe) throws Exception;

}
