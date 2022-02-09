const api = '/graphql';
const ui = '/graphql-ui';
const logo = '/graphql-ui';
const defaultQuery = "";
const headerEditorEnabled = true;
const shouldPersistHeaders = false;

var webSocket = null;
var observable = null;

window.onbeforeunload = function (event) {
    if(webSocket !==null){
        webSocket.close();
        webSocket = null;
        observable = null;
    }
};


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
    
    var query = graphQLParams.query;
    
    if(query.startsWith("subscription ")){
        var new_uri = getWsUrl();
        var initialized = false;
        observable = new rxjs.Observable((observer) => {
            webSocket = new WebSocket(url = new_uri, protocols = "graphql-transport-ws");
            webSocket.onopen = function() {
                observer.next("Initializing a connection to the server...");
                webSocket.send(JSON.stringify({type: "connection_init"}));
            };
            webSocket.onmessage = function (event) {
                let data = JSON.parse(event.data);
                switch(data["type"]) {
                    case 'connection_ack':
                        initialized = true;
                        let startMessage = {
                            id: "1",
                            type: "subscribe",
                            payload: graphQLParams
                        };
                        webSocket.send(JSON.stringify(startMessage));
                        observer.next("Connection initialized, requested a subscription...")
                        break;
                    case 'next':
                        observer.next(data.payload);
                        break;
                    case 'complete':
                        webSocket.close();
                        break;
                    default:
                        observer.next(data);
                        break;
                }
            };
            webSocket.onerror = function(err) {
                observer.error(JSON.stringify(err, null, 4));
            };
            webSocket.onclose = function(event){
                observer.complete();
                observable = null;
            };
            return {
                unsubscribe() {
                    if(initialized) {
                        webSocket.send(JSON.stringify({
                            id: "1",
                            type: "complete"
                        }));
                    }
                    webSocket.close();
                    webSocket = null;
                    observable = null;
                }
            };
          });
          return observable;
    }else{
        return fetch(api, {
            method: 'post',
            headers: mergedHeaders,
            body: JSON.stringify(graphQLParams),
        }).then(function (response) {
            return response.text();
        }).then(function (responseBody) {
            try {
                return JSON.parse(responseBody);
            } catch (error) {
                return responseBody;
            }
        });
    }
}

function getWsUrl(){
    var new_uri;
    if (window.location.protocol === "https:") {
        new_uri = "wss:";
    } else {
        new_uri = "ws:";
    }
    new_uri += "//" + window.location.host + api;
    
    return new_uri;
}

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

document.getElementsByClassName("title")[0].innerHTML = "<a id='graphQLUiLogoLink' href='" + logo + "'><img src='logo.png' alt='SmallRye Graphql' height='44' align='middle'></a><a id='graphQLUiTitleLink' href='" + ui + "'></a>";