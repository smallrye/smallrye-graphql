const ui = '/graphql-ui';
const defaultQuery = "";
const headerEditorEnabled = true;
const shouldPersistHeaders = false;

let path = new URL(window.location.href).pathname
if (path.endsWith("/")) {
    path = path.substr(0, path.length - 1);
}
if (path.endsWith(ui)) {
    path = path.substr(0, path.length - ui.length);
}

const api = path + '/graphql';

// Parse the search string to get url parameters.
var search = window.location.search;
var parameters = {};
search
        .substr(1)
        .split('&')
        .forEach(function (entry) {
            var eq = entry.indexOf('=');
            if (eq >= 0) {
                parameters[decodeURIComponent(entry.slice(0, eq))] = decodeURIComponent(
                        entry.slice(eq + 1),
                        );
            }
        });

// If variables was provided, try to format it.
if (parameters.variables) {
    try {
        parameters.variables = JSON.stringify(
                JSON.parse(parameters.variables),
                null,
                2,
                );
    } catch (e) {
        // Do nothing, we want to display the invalid JSON as a string, rather
        // than present an error.
    }
}

// If headers was provided, try to format it.
if (parameters.headers) {
    try {
        parameters.headers = JSON.stringify(
                JSON.parse(parameters.headers),
                null, 2, );
    } catch (e) {
        // Do nothing, we want to display the invalid JSON as a string, rather
        // than present an error.
    }
}

// When the query and variables string is edited, update the URL bar so
// that it can be easily shared.
function onEditQuery(newQuery) {
    parameters.query = newQuery;
    updateURL();
}

function onEditVariables(newVariables) {
    parameters.variables = newVariables;
    updateURL();
}

function onEditOperationName(newOperationName) {
    parameters.operationName = newOperationName;
    updateURL();
}

function onEditHeaders(newHeaders) {
    parameters.headers = newHeaders;
    updateURL();
}

function updateURL() {
    var newSearch =
            '?' +
            Object.keys(parameters)
            .filter(function (key) {
                return Boolean(parameters[key]);
            })
            .map(function (key) {
                return (
                        encodeURIComponent(key) + '=' + encodeURIComponent(parameters[key])
                        );
            })
            .join('&');
    history.replaceState(null, null, newSearch);
}

var defaultHeaders = {
    "Accept": "application/json",
    "Content-Type": "application/json"
};

// Defines a GraphQL fetcher using the fetch API. You're not required to
// use fetch, and could instead implement graphQLFetcher however you like,
// as long as it returns a Promise or Observable.
function graphQLFetcher(graphQLParams) {
    let mergedHeaders;
    if (typeof parameters.headers === "undefined" || parameters.headers === null || parameters.headers.trim() === "") {
        mergedHeaders = defaultHeaders;
    } else {
        mergedHeaders = {
            ...defaultHeaders,
            ...JSON.parse(parameters.headers)
        };
    }

    return fetch(api, {
        method: 'post',
        headers: mergedHeaders,
        body: JSON.stringify(graphQLParams),
    })
            .then(function (response) {
                return response.text();
            })
            .then(function (responseBody) {
                try {
                    return JSON.parse(responseBody);
                } catch (error) {
                    return responseBody;
                }
            });
}

// Render <GraphiQL /> into the body.
// See the README in the top level of this module to learn more about
// how you can customize GraphiQL by providing different values or
// additional child elements.
ReactDOM.render(
        React.createElement(GraphiQL, {
            fetcher: graphQLFetcher,
            query: parameters.query,
            variables: parameters.variables,
            headers: parameters.headers,
            operationName: parameters.operationName,
            onEditQuery: onEditQuery,
            onEditVariables: onEditVariables,
            onEditHeaders: onEditHeaders,
            defaultSecondaryEditorOpen: true,
            onEditOperationName: onEditOperationName,
            headerEditorEnabled: headerEditorEnabled,
            shouldPersistHeaders: shouldPersistHeaders,
            defaultQuery: defaultQuery
        }),
        document.getElementById('graphiql'),
        );

document.getElementsByClassName("title")[0].innerHTML = "<a href='" + ui + "'><img src='logo.png' alt='SmallRye Graphql' height='44' align='middle'></a>";
