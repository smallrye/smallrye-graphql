import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.ArrayList;
import java.util.Enumeration;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.Set;
import java.util.TreeMap;
import java.util.TreeSet;
import java.util.jar.JarEntry;
import java.util.jar.JarFile;
import java.util.stream.Stream;

import javax.xml.parsers.DocumentBuilderFactory;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

import io.mvnpm.esbuild.Bundler;
import io.mvnpm.esbuild.model.BundleOptions;
import io.mvnpm.esbuild.model.BundleOptionsBuilder;
import io.mvnpm.esbuild.model.BundleResult;
import io.mvnpm.esbuild.model.EsBuildConfig;
import io.mvnpm.esbuild.model.WebDependency;

/**
 * Bundles the GraphiQL UI with esbuild at build time, so that the UI does not need to load anything from a CDN.
 * The JavaScript dependencies are taken from the mvnpm jars on the classpath.
 * <p>
 * Before bundling, it verifies that the resolved mvnpm dependencies match the 'locker' profile in the pom, so that
 * changing a dependency version (for example by Dependabot) without re-generating the lock fails the build.
 * <p>
 * Launched from the pom as a single-file source program:
 * {@code java -cp <classpath> Bundle.java <pom> <sourceDir> <outputDir> <workDir> <entryPoint>...}
 */
public class Bundle {

    public static void main(String[] args) throws Exception {
        Path pom = Path.of(args[0]).toAbsolutePath();
        Path sourceDir = Path.of(args[1]).toAbsolutePath();
        Path outputDir = Path.of(args[2]).toAbsolutePath();
        Path workDir = Path.of(args[3]).toAbsolutePath();

        Map<String, Path> jars = webDependencyJars();
        checkLock(pom, jars.keySet());

        // esbuild requires node_modules to be in an ancestor of the sources, so work on a copy in the target directory
        Path workSourceDir = workDir.resolve("src");
        copy(sourceDir, workSourceDir);

        List<WebDependency> dependencies = new ArrayList<>();
        for (Path jar : jars.values()) {
            dependencies.add(WebDependency.of(jar, WebDependency.WebDependencyType.MVNPM));
        }
        BundleOptionsBuilder options = BundleOptions.builder()
                .withWorkDir(workSourceDir)
                .withNodeModulesDir(workDir.resolve("node_modules"))
                .withDependencies(dependencies)
                .withEsConfig(EsBuildConfig.builder()
                        .fixedEntryNames()
                        .splitting(false)
                        .sourceMap(false)
                        .build());
        for (int i = 4; i < args.length; i++) {
            options.addEntryPoint(args[i]);
        }

        BundleResult result = Bundler.bundle(options.build(), true);
        copy(result.dist(), outputDir);
    }

    /**
     * The mvnpm jars on the classpath, keyed by 'groupId:artifactId:version'.
     */
    private static Map<String, Path> webDependencyJars() throws IOException {
        Map<String, Path> jars = new TreeMap<>();
        for (String entry : System.getProperty("java.class.path").split(File.pathSeparator)) {
            if (!entry.endsWith(".jar")) {
                continue;
            }
            String gav = readGav(Path.of(entry));
            if (gav != null && isWebDependency(gav)) {
                jars.put(gav, Path.of(entry));
            }
        }
        return jars;
    }

    private static boolean isWebDependency(String gav) {
        // skip esbuild and deno themselves, they are dependencies of the bundler (and platform specific), not of the UI
        return gav.startsWith("org.mvnpm")
                && !gav.startsWith("org.mvnpm:esbuild:")
                && !gav.startsWith("org.mvnpm.at.esbuild:")
                && !gav.startsWith("org.mvnpm.at.deno:");
    }

    private static String readGav(Path jar) throws IOException {
        try (JarFile jarFile = new JarFile(jar.toFile())) {
            Enumeration<JarEntry> entries = jarFile.entries();
            while (entries.hasMoreElements()) {
                JarEntry entry = entries.nextElement();
                if (entry.getName().startsWith("META-INF/maven/") && entry.getName().endsWith("/pom.properties")) {
                    Properties properties = new Properties();
                    try (InputStream in = jarFile.getInputStream(entry)) {
                        properties.load(in);
                    }
                    return properties.getProperty("groupId") + ":" + properties.getProperty("artifactId") + ":"
                            + properties.getProperty("version");
                }
            }
        }
        return null;
    }

    /**
     * Fails if the resolved web dependencies differ from the ones pinned in the 'locker' profile.
     */
    private static void checkLock(Path pom, Set<String> resolved) throws Exception {
        Document document = DocumentBuilderFactory.newInstance().newDocumentBuilder().parse(pom.toFile());
        TreeSet<String> locked = new TreeSet<>();
        NodeList profiles = document.getElementsByTagName("profile");
        for (int i = 0; i < profiles.getLength(); i++) {
            Element profile = (Element) profiles.item(i);
            if (!"locker".equals(childText(profile, "id"))) {
                continue;
            }
            NodeList dependencies = profile.getElementsByTagName("dependency");
            for (int j = 0; j < dependencies.getLength(); j++) {
                Element dependency = (Element) dependencies.item(j);
                String gav = childText(dependency, "groupId") + ":" + childText(dependency, "artifactId") + ":"
                        + childText(dependency, "version");
                if (isWebDependency(gav)) {
                    locked.add(gav);
                }
            }
        }

        TreeSet<String> notLocked = new TreeSet<>(resolved);
        notLocked.removeAll(locked);
        TreeSet<String> notResolved = new TreeSet<>(locked);
        notResolved.removeAll(resolved);
        if (!notLocked.isEmpty() || !notResolved.isEmpty()) {
            System.err.println("The 'locker' profile in " + pom + " is out of date.");
            System.err.println("Resolved, but not locked: " + notLocked);
            System.err.println("Locked, but not resolved: " + notResolved);
            System.err.println("Re-generate it by running this in the ui/graphiql directory:");
            System.err.println("    mvn io.mvnpm:locker-maven-plugin:1.0.1:lock -Dunlocked");
            System.exit(1);
        }
    }

    private static String childText(Element element, String name) {
        NodeList children = element.getChildNodes();
        for (int i = 0; i < children.getLength(); i++) {
            if (children.item(i) instanceof Element child && child.getTagName().equals(name)) {
                return child.getTextContent().trim();
            }
        }
        return null;
    }

    private static void copy(Path from, Path to) throws IOException {
        try (Stream<Path> files = Files.walk(from)) {
            for (Path file : (Iterable<Path>) files::iterator) {
                Path target = to.resolve(from.relativize(file).toString());
                if (Files.isDirectory(file)) {
                    Files.createDirectories(target);
                } else {
                    Files.copy(file, target, StandardCopyOption.REPLACE_EXISTING);
                }
            }
        }
    }
}
