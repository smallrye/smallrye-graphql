package io.smallrye.graphql.client.generator;

import static java.util.Collections.emptyList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.stream.Collector;
import java.util.stream.Stream;

import graphql.language.Argument;
import graphql.language.Document;
import graphql.language.Field;
import graphql.language.FieldDefinition;
import graphql.language.ListType;
import graphql.language.NonNullType;
import graphql.language.ObjectTypeDefinition;
import graphql.language.OperationDefinition;
import graphql.language.SelectionSet;
import graphql.language.Type;
import graphql.language.TypeName;
import graphql.language.Value;
import graphql.language.VariableDefinition;
import graphql.language.VariableReference;
import graphql.parser.Parser;
import graphql.schema.idl.SchemaParser;
import graphql.schema.idl.TypeDefinitionRegistry;

public class Generator {
    private final String pkg;
    private final String apiTypeName;
    private final String schemaString;
    private final List<String> queryStrings;

    private TypeDefinitionRegistry schema;
    private List<Document> queries;

    public Generator(String pkg, String apiTypeName, String schema, List<String> queries) {
        this.pkg = pkg;
        this.apiTypeName = apiTypeName;
        this.schemaString = schema;
        this.queryStrings = queries;
    }

    public Map<String, String> generateSourceFiles() {
        this.schema = parseSchema();
        this.queries = parseQueries();
        Map<String, String> sourceFiles = new LinkedHashMap<>();

        new Api().addTo(sourceFiles);

        return sourceFiles;
    }

    private TypeDefinitionRegistry parseSchema() {
        try {
            return new SchemaParser().parse(schemaString);
        } catch (Exception e) {
            throw new GraphQlGeneratorException("can't parse schema: " + schemaString, e);
        }
    }

    private List<Document> parseQueries() {
        return queryStrings.stream()
                .map(this::query)
                .collect(toList());
    }

    private Document query(String query) {
        try {
            return Parser.parse(query);
        } catch (Exception e) {
            throw new GraphQlGeneratorException("can't parse query: " + query, e);
        }
    }

    private abstract class SourceFileGenerator {
        protected final String typeType;

        protected final Set<String> imports = new TreeSet<>();

        protected abstract String generateBody();

        final List<SourceFileGenerator> other = new ArrayList<>();

        public SourceFileGenerator(String typeType) {
            this.typeType = typeType;
        }

        public abstract String getTypeName();

        protected class JavaType {
            private final Type<?> type;
            private final List<String> fieldNames;

            public JavaType(Type<?> type, List<String> fieldNames) {
                this.type = type;
                this.fieldNames = fieldNames;
            }

            @Override
            public String toString() {
                if (type instanceof ListType)
                    return toListJava((ListType) type);
                if (type instanceof NonNullType)
                    return toNonNullJava((NonNullType) type);
                if (type instanceof TypeName)
                    return toJava((TypeName) type);
                throw new UnsupportedOperationException("unexpected type of type"); // unreachable
            }

            private String toListJava(ListType type) {
                imports.add("java.util.List");
                return "List<" + new JavaType(type.getType(), fieldNames) + ">";
            }

            private String toNonNullJava(NonNullType type) {
                imports.add("org.eclipse.microprofile.graphql.NonNull");
                return "@NonNull " + new JavaType(type.getType(), fieldNames);
            }

            private String toJava(TypeName typeName) {
                String string = typeName.getName();
                switch (string) {
                    case "Int":
                        return "Integer";
                    case "Boolean":
                    case "Float":
                    case "String":
                        return string;
                    case "ID":
                        return "String";
                    default:
                        other.add(new TypeGenerator(string, fieldNames));
                        return string;
                }
            }
        }

        public void addTo(Map<String, String> sourceFiles) {
            String body = generateBody();
            String source = "package " + pkg + ";\n" +
                    "\n" +
                    imports() +
                    "public " + typeType + " " + getTypeName() + " {\n" +
                    body +
                    "}\n";
            String previousSource = sourceFiles.put(pkg + "." + getTypeName(), source);
            if (previousSource != null && !previousSource.equals(source))
                throw new GraphQlGeneratorException("already generated " + getTypeName());
            other.forEach(it -> it.addTo(sourceFiles));
        }

        private String imports() {
            return imports.isEmpty() ? "" : imports.stream().collect(joining(";\nimport ", "import ", ";\n\n"));
        }
    }

    private class Api extends SourceFileGenerator {
        private final StringBuilder body = new StringBuilder();

        public Api() {
            super("interface");
        }

        @Override
        public String getTypeName() {
            return apiTypeName;
        }

        @Override
        protected String generateBody() {
            queries.forEach(this::generateQueryMethod);

            return body.toString();
        }

        private void generateQueryMethod(Document query) {
            List<OperationDefinition> definitions = query.getDefinitionsOfType(OperationDefinition.class);
            if (definitions.size() != 1)
                throw new GraphQlGeneratorException("expected exactly one definition but found "
                        + definitions.stream().map(this::operationInfo).collect(listString()));
            OperationDefinition operation = definitions.get(0);
            List<Field> fields = operation.getSelectionSet().getSelectionsOfType(Field.class);
            if (fields.size() != 1)
                throw new GraphQlGeneratorException("expected exactly one field but got "
                        + fields.stream().map(Field::getName).collect(listString()));
            Field field = fields.get(0);
            body.append(new MethodGenerator(operation, field));
        }

        private String operationInfo(OperationDefinition definition) {
            return definition.getOperation().toString().toLowerCase() + " " + definition.getName();
        }

        private class MethodGenerator {
            private final OperationDefinition operation;
            private final Field method;

            private final StringBuilder annotations = new StringBuilder();

            public MethodGenerator(OperationDefinition operation, Field method) {
                this.operation = operation;
                this.method = method;
            }

            @Override
            public String toString() {
                JavaType returnType = returnType();
                String methodName = methodName();
                String argumentList = argumentList();
                return "    " + this.annotations + returnType + " " + methodName + argumentList + ";\n";
            }

            private JavaType returnType() {
                ObjectTypeDefinition query = (ObjectTypeDefinition) schema.getType("Query")
                        .orElseThrow(() -> new GraphQlGeneratorException("'Query' type not found in schema"));
                FieldDefinition fieldDefinition = query.getFieldDefinitions().stream()
                        .filter(f -> f.getName().equals(method.getName()))
                        .findAny()
                        .orElseThrow(() -> new GraphQlGeneratorException("field (method) '" + method.getName()
                                + "' not found in "
                                + query.getFieldDefinitions().stream().map(FieldDefinition::getName).collect(listString())));
                List<String> selections = operation.getSelectionSet().getSelectionsOfType(Field.class).stream()
                        .flatMap(this::selectedFields)
                        .map(Field::getName)
                        .collect(toList());
                return new JavaType(fieldDefinition.getType(), selections);
            }

            private Stream<? extends Field> selectedFields(Field field) {
                SelectionSet selectionSet = field.getSelectionSet();
                return (selectionSet == null) ? Stream.of() : selectionSet.getSelectionsOfType(Field.class).stream();
            }

            public String methodName() {
                if (method.getAlias() != null) {
                    nameQuery();
                    return method.getAlias();
                }
                if (operation.getName() == null)
                    return method.getName();
                if (!operation.getName().equals(method.getName()))
                    nameQuery();
                return operation.getName();
            }

            private void nameQuery() {
                imports.add("org.eclipse.microprofile.graphql.Query");
                annotations.append("@Query(\"").append(method.getName()).append("\") ");
            }

            public String argumentList() {
                return new ArgumentList(method, operation.getVariableDefinitions()).toString();
            }

            private class ArgumentList {
                private final Field field;
                private final List<VariableDefinition> variableDefinitions;

                public ArgumentList(Field field, List<VariableDefinition> variableDefinitions) {
                    this.field = field;
                    this.variableDefinitions = variableDefinitions;
                }

                @Override
                public String toString() {
                    return field.getArguments().stream()
                            .map(argument -> new JavaType(type(argument), emptyList()) + " " + argument.getName())
                            .collect(joining(", ", "(", ")"));
                }

                private Type<?> type(Argument argument) {
                    Value<?> value = argument.getValue();
                    if (value instanceof VariableReference)
                        return resolve(((VariableReference) value).getName());
                    throw new GraphQlGeneratorException(
                            "unsupported type " + value + " for argument '" + argument.getName() + "'");
                }

                private Type<?> resolve(String name) {
                    return variableDefinitions.stream()
                            .filter(var -> var.getName().equals(name))
                            .findAny()
                            .map(VariableDefinition::getType)
                            .orElseThrow(() -> new GraphQlGeneratorException("no definition found for parameter '" + name
                                    + "' in "
                                    + variableDefinitions.stream().map(VariableDefinition::getName).collect(listString())));
                }
            }
        }
    }

    private class TypeGenerator extends SourceFileGenerator {
        private final String typeName;
        private final List<String> fieldNames;

        public TypeGenerator(String typeName, List<String> fieldNames) {
            super("class");
            this.typeName = typeName;
            this.fieldNames = fieldNames;
        }

        @Override
        public String getTypeName() {
            return typeName;
        }

        @Override
        protected String generateBody() {
            ObjectTypeDefinition typeDefinition = (ObjectTypeDefinition) schema.getType(typeName)
                    .orElseThrow(() -> new GraphQlGeneratorException("type '" + typeName + "' not found in schema"));
            return typeDefinition.getFieldDefinitions().stream()
                    .filter(field -> fieldNames.contains(field.getName()))
                    .map(this::toJava)
                    .collect(joining(";\n    ", "    ", ";\n"));
        }

        private String toJava(FieldDefinition field) {
            return new JavaType(field.getType(), fieldNames) + " " + field.getName();
        }
    }

    private static Collector<CharSequence, ?, String> listString() {
        return joining(", ", "[", "]");
    }
}
