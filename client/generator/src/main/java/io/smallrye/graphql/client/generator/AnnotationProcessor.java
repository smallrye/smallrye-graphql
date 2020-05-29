package io.smallrye.graphql.client.generator;

import static java.util.stream.Collectors.toList;
import static javax.tools.Diagnostic.Kind.ERROR;
import static javax.tools.StandardLocation.CLASS_OUTPUT;

import java.io.IOException;
import java.io.InputStream;
import java.io.Writer;
import java.net.URI;
import java.nio.file.Paths;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Scanner;
import java.util.Set;
import java.util.stream.Stream;

import javax.annotation.processing.AbstractProcessor;
import javax.annotation.processing.RoundEnvironment;
import javax.annotation.processing.SupportedAnnotationTypes;
import javax.lang.model.SourceVersion;
import javax.lang.model.element.Element;
import javax.lang.model.element.PackageElement;
import javax.lang.model.element.TypeElement;
import javax.tools.FileObject;
import javax.tools.JavaFileObject;

@SupportedAnnotationTypes({
        "io.smallrye.graphql.client.generator.GraphqlQuery",
        "io.smallrye.graphql.client.generator.GraphqlQueries" })
public class AnnotationProcessor extends AbstractProcessor {
    @Override
    public SourceVersion getSupportedSourceVersion() {
        return SourceVersion.latest();
    }

    @Override
    public boolean process(Set<? extends TypeElement> annotations, RoundEnvironment roundEnv) {
        // getElementsAnnotatedWithAny is Java 9+
        Set<Element> all = new LinkedHashSet<>();
        all.addAll(roundEnv.getElementsAnnotatedWith(GraphqlQuery.class));
        all.addAll(roundEnv.getElementsAnnotatedWith(GraphqlQueries.class));
        all.forEach(this::processAnnotatedType);
        return true;
    }

    private void processAnnotatedType(Element element) {
        info("processing " + element);
        generateApiFor((TypeElement) element);
    }

    private void info(String message) {
        System.out.println("[INFO] " + message);
    }

    private void generateApiFor(TypeElement type) {
        String pkg = packageOf(type).getQualifiedName().toString();
        String apiTypeName = type.getSimpleName() + "Api";
        String schema = readSchema(type);
        if (schema == null)
            return;
        List<String> queries = Stream.of(type.getAnnotationsByType(GraphqlQuery.class))
                .map(GraphqlQuery::value)
                .collect(toList());

        Generator generator = new Generator(pkg, apiTypeName, schema, queries);

        try {
            generator.generateSourceFiles().forEach(this::writeJavaSource);
        } catch (Exception e) {
            StringBuilder messages = new StringBuilder();
            for (Throwable t = e; t != null; t = t.getCause())
                messages.append(t.getMessage());
            processingEnv.getMessager().printMessage(ERROR, messages.toString(), type);
        }
    }

    private PackageElement packageOf(Element start) {
        for (Element element = start; element != null; element = element.getEnclosingElement())
            if (element instanceof PackageElement)
                return (PackageElement) element;
        throw new RuntimeException("element " + start + " is not enclosed in a package");
    }

    private String readSchema(TypeElement type) {
        GraphQlSchema annotation = type.getAnnotation(GraphQlSchema.class);
        if (annotation == null) {
            processingEnv.getMessager().printMessage(ERROR, "missing GraphQlSchema annotation", type);
            return null;
        }

        String schemaLocation = annotation.value();
        try {
            return read(schemaLocation, targetClassesPath(type));
        } catch (IOException e) {
            processingEnv.getMessager().printMessage(ERROR, "can't read from " + schemaLocation
                    + ": " + e.getClass().getName() + ": " + e.getMessage(), type);
            return null;
        }
    }

    private String targetClassesPath(TypeElement type) throws IOException {
        String qualified = type.getQualifiedName().toString();
        int lastDot = qualified.lastIndexOf('.');
        String pkg = qualified.substring(0, lastDot);
        String cls = qualified.substring(lastDot + 1);
        FileObject someResource = processingEnv.getFiler().getResource(CLASS_OUTPUT, pkg, cls + ".java");
        String someSourcePath = qualified.replace('.', '/') + ".java";
        int index = someResource.getName().indexOf(someSourcePath);
        return someResource.getName().substring(0, index);
    }

    private String read(String location, String targetClasses) throws IOException {
        URI uri = (location.startsWith("resource:"))
                ? Paths.get(targetClasses + location.substring(9)).toUri()
                : URI.create(location);

        InputStream inputStream = uri.toURL().openConnection().getInputStream();
        try (Scanner scanner = new Scanner(inputStream).useDelimiter("\\Z")) {
            return scanner.next();
        }
    }

    private void writeJavaSource(String fileName, String sourceCode) {
        info("writing " + fileName);
        try {
            JavaFileObject javaFileObject = processingEnv.getFiler().createSourceFile(fileName);
            try (Writer writer = javaFileObject.openWriter()) {
                writer.write(sourceCode);
            }
        } catch (IOException e) {
            throw new RuntimeException("can't write " + fileName, e);
        }
    }
}
