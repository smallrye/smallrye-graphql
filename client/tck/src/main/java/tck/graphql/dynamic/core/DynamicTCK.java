package tck.graphql.dynamic.core;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("tck.graphql.dynamic.core")
@IncludeClassNamePatterns("^(Test.*|.+[.$]Test.*|.*Tests?|.*Behavior)$")
public abstract class DynamicTCK {
}
