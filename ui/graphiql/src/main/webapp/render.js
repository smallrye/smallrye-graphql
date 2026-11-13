// Not bundled: these lines are rewritten after the build, so they must stay one per line and in this form.
// Quarkus replaces each line starting with "const api = '", "const ui = '" and "const logo = '" by its configured path.
const api = '/graphql';
const ui = '/graphql-ui';
const logo = '/graphql-ui';

globalThis.smallRyeGraphQLUi = { api: api, ui: ui, logo: logo };

// The GraphiQL UI, bundled with all its dependencies (see src/main/web/graphiql.js)
await import('./graphiql.js');
