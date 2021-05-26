package tck.graphql.typesafe;

import org.junit.platform.suite.api.IncludeClassNamePatterns;
import org.junit.platform.suite.api.SelectPackages;
import org.junit.platform.suite.api.Suite;

@Suite
@SelectPackages("tck.graphql.typesafe")
@IncludeClassNamePatterns("^(Test.*|.+[.$]Test.*|.*Tests?|.*Behavior)$")
public abstract class TypesafeTCK {
}
