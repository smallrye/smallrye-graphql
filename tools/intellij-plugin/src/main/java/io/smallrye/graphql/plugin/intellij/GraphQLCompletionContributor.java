package io.smallrye.graphql.plugin.intellij;

import static com.intellij.patterns.PsiJavaPatterns.psiElement;
import static java.util.stream.Collectors.toSet;

import java.util.Optional;
import java.util.Set;
import java.util.logging.Logger;
import java.util.stream.Stream;

import org.jetbrains.annotations.NotNull;

import com.intellij.codeInsight.completion.CompletionContributor;
import com.intellij.codeInsight.completion.CompletionParameters;
import com.intellij.codeInsight.completion.CompletionProvider;
import com.intellij.codeInsight.completion.CompletionResultSet;
import com.intellij.codeInsight.completion.CompletionType;
import com.intellij.codeInsight.hint.HintManager;
import com.intellij.codeInsight.lookup.LookupElementBuilder;
import com.intellij.openapi.application.ApplicationManager;
import com.intellij.openapi.editor.Editor;
import com.intellij.psi.PsiClass;
import com.intellij.psi.PsiField;
import com.intellij.psi.PsiIdentifier;
import com.intellij.psi.PsiJavaCodeReferenceElement;
import com.intellij.psi.PsiMethod;
import com.intellij.psi.PsiNamedElement;
import com.intellij.psi.PsiWhiteSpace;
import com.intellij.util.ProcessingContext;

import io.smallrye.graphql.plugin.intellij.Schema.DefinedField;

public class GraphQLCompletionContributor extends CompletionContributor {
    public GraphQLCompletionContributor() {
        extend(CompletionType.BASIC, psiElement(), new GraphQLCompletionProvider());
    }

    private static class GraphQLCompletionProvider extends CompletionProvider<CompletionParameters> {
        private static final Logger LOG = Logger.getLogger(GraphQLCompletionProvider.class.getName());
        private Editor editor;

        @Override
        protected void addCompletions(@NotNull CompletionParameters parameters, @NotNull ProcessingContext context,
                @NotNull CompletionResultSet result) {
            this.editor = parameters.getEditor();
            var schema = schema();
            graphQLClientApi(parameters).ifPresent(api -> {
                Set<String> existingMethods = existing(api, PsiMethod.class);
                Stream.concat(
                        schema.fieldsIn("Query"),
                        schema.fieldsIn("Mutation"))
                        .filter(query -> !existingMethods.contains(query.getName()))
                        .forEach(query -> addLookupElement(result, query.javaMethod(), query));
            });
            graphQLType(parameters).ifPresent(type -> {
                Set<String> existingFields = existing(type, PsiField.class);
                schema.fieldsIn(type.getName())
                        .filter(field -> !existingFields.contains(field.getName()))
                        .forEach(field -> addLookupElement(result, field.javaField(), field));
            });
        }

        private Optional<PsiClass> graphQLClientApi(CompletionParameters parameters) {
            var api = findApi(parameters);
            if (api == null || !api.isInterface())
                return Optional.empty();
            var graphQlClientApiAnnotation = api.getAnnotation("io.smallrye.graphql.client.typesafe.api.GraphQLClientApi");
            if (graphQlClientApiAnnotation == null)
                return Optional.empty();
            LOG.fine("found GraphQLClientApi " + api.getName());
            return Optional.of(api);
        }

        private Optional<PsiClass> graphQLType(CompletionParameters parameters) {
            var api = findApi(parameters);
            if (api == null || api.isInterface())
                return Optional.empty();
            if (!schema().typeNames().contains(api.getName()))
                return Optional.empty();
            LOG.fine("found GraphQL type " + api.getName());
            return Optional.of(api);
        }

        private PsiClass findApi(CompletionParameters parameters) {
            var element = parameters.getOriginalPosition();
            // user already typed a GraphQL field/method name
            if (element instanceof PsiIdentifier && element.getParent() instanceof PsiJavaCodeReferenceElement) {
                for (; element.getParent() != null; element = element.getParent()) {
                    if (element instanceof PsiClass) {
                        return (PsiClass) element;
                    }
                }
            } else // user manually started autocomplete at whitespace
            if (element instanceof PsiWhiteSpace && element.getParent() instanceof PsiClass) {
                return (PsiClass) element.getParent();
            }
            return null;
        }

        private Set<String> existing(PsiClass api, Class<? extends PsiNamedElement> type) {
            return Stream.of(api.getChildren())
                    .filter(type::isInstance)
                    .map(element -> ((PsiNamedElement) element).getName())
                    .collect(toSet());
        }

        private Schema schema() {
            return Schema.INSTANCE.withErrors(this::error);
        }

        private void error(String message) {
            ApplicationManager.getApplication().invokeLater(() -> HintManager.getInstance().showErrorHint(editor, message));
        }

        private static void addLookupElement(CompletionResultSet result, String name, DefinedField field) {
            result.addElement(LookupElementBuilder.create(name)
                    .withPresentableText(field.getName())
                    .withTailText(" " + field.getDescription())
                    .withTypeText("GraphQL " + field.getDefiningType()));
        }
    }
}
