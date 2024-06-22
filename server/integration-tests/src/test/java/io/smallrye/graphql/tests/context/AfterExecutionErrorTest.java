package io.smallrye.graphql.tests.context;

import static org.junit.Assert.assertEquals;

import java.net.URL;
import java.util.List;

import org.eclipse.microprofile.graphql.GraphQLApi;
import org.eclipse.microprofile.graphql.Query;
import org.jboss.arquillian.container.test.api.Deployment;
import org.jboss.arquillian.junit.Arquillian;
import org.jboss.arquillian.test.api.ArquillianResource;
import org.jboss.shrinkwrap.api.ShrinkWrap;
import org.jboss.shrinkwrap.api.asset.StringAsset;
import org.jboss.shrinkwrap.api.spec.WebArchive;
import org.junit.Test;
import org.junit.runner.RunWith;

import graphql.ErrorClassification;
import graphql.ExecutionResult;
import graphql.ExecutionResultImpl;
import graphql.GraphQLError;
import graphql.language.SourceLocation;
import io.smallrye.graphql.api.Context;
import io.smallrye.graphql.execution.context.SmallRyeContext;
import io.smallrye.graphql.spi.EventingService;
import io.smallrye.graphql.tests.GraphQLAssured;

@RunWith(Arquillian.class)
public class AfterExecutionErrorTest {

    @GraphQLApi
    public static class SomeApi {

        @Query
        public String foo() {
            return "bar";
        }
    }

    public static class Events implements EventingService {

        @Override
        public String getConfigKey() {
            return null;
        }

        @Override
        public void afterExecute(Context context) {
            ExecutionResult executionResult = ((SmallRyeContext) context).unwrap(ExecutionResult.class);
            ExecutionResult newExecutionResult = ExecutionResultImpl
                    .newExecutionResult()
                    .from(executionResult)
                    .errors(List.of(new GraphQLError() {
                        @Override
                        public String getMessage() {
                            return "Error occurred in afterExecute hook";
                        }

                        @Override
                        public List<SourceLocation> getLocations() {
                            return List.of();
                        }

                        @Override
                        public ErrorClassification getErrorType() {
                            return null;
                        }
                    })).build();
            ((SmallRyeContext) context).setExecutionResult(newExecutionResult);
        }
    }

    @Deployment
    public static WebArchive deployment() {
        return ShrinkWrap.create(WebArchive.class, "after-execute-test.war")
                .addAsResource(new StringAsset(Events.class.getName()),
                        "META-INF/services/io.smallrye.graphql.spi.EventingService")
                .addClasses(SomeApi.class);
    }

    @ArquillianResource
    URL testingURL;

    @Test
    public void executionResultContainsErrorTest() {
        GraphQLAssured graphQLAssured = new GraphQLAssured(testingURL);
        String response = graphQLAssured.post("{ foo }");
        assertEquals("{\"errors\":[{\"message\":\"Error occurred in afterExecute hook\",\"locations\":[]}]" +
                ",\"data\":{\"foo\":\"bar\"}}", response);
    }
}
