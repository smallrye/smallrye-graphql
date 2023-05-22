const api = '/graphql';
const logo = '/graphql-ui';
const defaultQuery = '';
const headerEditorEnabled = true;
const shouldPersistHeaders = false;

const urlParams = new URLSearchParams(window.location.search);
const embed = urlParams.get('embed');

// Parse the search string to get url parameters.
var search = window.location.search;
var parameters = {};
search
	.substr(1)
	.split('&')
	.forEach(function (entry) {
		var eq = entry.indexOf('=');
		if (eq >= 0) {
			parameters[decodeURIComponent(entry.slice(0, eq))] =
				decodeURIComponent(entry.slice(eq + 1));
		}
	});

// If variables was provided, try to format it.
if (parameters.variables) {
	try {
		parameters.variables = JSON.stringify(
			JSON.parse(parameters.variables),
			null,
			2
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
			null,
			2
		);
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

function onEditHeaders(newHeaders) {
	parameters.headers = newHeaders;
	updateURL();
}

function onEditOperationName(newOperationName) {
	parameters.operationName = newOperationName;
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
					encodeURIComponent(key) +
					'=' +
					encodeURIComponent(parameters[key])
				);
			})
			.join('&');
	history.replaceState(null, null, newSearch);
}

var defaultHeaders = {
	Accept: 'application/json',
	'Content-Type': 'application/json',
};

// Defines a GraphQL fetcher using the fetch API. You're not required to
// use fetch, and could instead implement graphQLFetcher however you like,
// as long as it returns a Promise or Observable.
function graphQLFetcher() {
	let mergedHeaders;
	if (
		typeof parameters.headers === 'undefined' ||
		parameters.headers === null ||
		parameters.headers.trim() === ''
	) {
		mergedHeaders = defaultHeaders;
	} else {
		mergedHeaders = {
			...defaultHeaders,
			...JSON.parse(parameters.headers),
		};
	}
	return GraphiQL.createFetcher({
		url: getUrl(),
		subscriptionUrl: getWsUrl(),
		headers: mergedHeaders,
	});
}

function getWsUrl() {
	var new_uri;
	if (window.location.protocol === 'https:') {
		new_uri = 'wss:';
	} else {
		new_uri = 'ws:';
	}
	new_uri += '//' + window.location.host + api;

	return new_uri;
}

function getUrl() {
	return window.location.protocol + '//' + window.location.host + api;
}

ReactDOM.render(
	React.createElement(GraphiQL, {
		fetcher: graphQLFetcher(),
		query: parameters.query,
		variables: parameters.variables,
		headers: parameters.headers,
		operationName: parameters.operationName,
		onEditOperationName: onEditOperationName,
		onEditVariables: onEditVariables,
		onEditHeaders: onEditHeaders,
		onEditQuery: onEditQuery,
		defaultSecondaryEditorOpen: true,
		headerEditorEnabled: headerEditorEnabled,
		shouldPersistHeaders: shouldPersistHeaders,
		defaultQuery: defaultQuery,
		defaultEditorToolsVisibility: true,
	}),
	document.getElementById('graphiql')
);

// LOGO (top-left corner)
if (!embed) {
	var sidebar = document.getElementsByClassName(
		'graphiql-sidebar-section'
	)[0];
	var logoLink = document.createElement('a');
	logoLink.id = 'graphQLUiLogoLink';
	logoLink.className = 'graphiql-un-styled';
	logoLink.href = logo;
	logoLink.innerHTML =
		"<img src='logo.png' alt='SmallRye Graphql' height='44' align='middle'>";
	sidebar.insertBefore(logoLink, sidebar.firstChild);
}
