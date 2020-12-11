package com.github.t1.powerannotations.scanner;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

class IndexerConfig {
    private final List<String> exclude = new ArrayList<>();

    IndexerConfig() {
        this("META-INF/jandex.properties");
    }

    IndexerConfig(String resource) {
        try (InputStream inputStream = ClassLoader.getSystemClassLoader().getResourceAsStream(resource)) {
            if (inputStream == null)
                return;
            Properties properties = new Properties();
            properties.load(inputStream);
            loadExcludeConfig(properties);
        } catch (IOException e) {
            throw new RuntimeException("can't load " + resource, e);
        }
    }

    private void loadExcludeConfig(Properties properties) {
        String excludeString = properties.getProperty("exclude", null);
        if (excludeString != null) {
            try {
                Stream.of(excludeString.split("\\s+"))
                        .map(this::gavToRegex)
                        .forEach(this.exclude::add);
            } catch (Exception e) {
                throw new RuntimeException("can't parse exclude config", e);
            }
        }
    }

    private String gavToRegex(String groupArtifact) {
        Matcher matcher = Pattern.compile("(?<group>[^:]+):(?<artifact>[^:]+)").matcher(groupArtifact);
        if (!matcher.matches())
            throw new RuntimeException("expect `group:artifact` but found `" + groupArtifact + "`");
        return ".*/" + matcher.group("group") + "/.*/" + matcher.group("artifact") + "-.*\\.jar";
    }

    public Stream<String> excludes() {
        return exclude.stream();
    }
}
