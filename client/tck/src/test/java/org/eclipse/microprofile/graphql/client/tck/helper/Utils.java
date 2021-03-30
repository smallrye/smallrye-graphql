package org.eclipse.microprofile.graphql.client.tck.helper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.stream.Collectors;

public class Utils {

    public static String getResourceFileContent(String resourceName) {
        ClassLoader classLoader = Thread.currentThread().getContextClassLoader();
        InputStream in = classLoader.getResourceAsStream(resourceName);
        BufferedReader reader = new BufferedReader(new InputStreamReader(in));
        String content = reader.lines().collect(Collectors.joining("\n"));

        return content;
    }

    private Utils() {
        // HideUtilityClassConstructor
    }
}
