import React from 'react';
import ReactDOM from 'react-dom/client';
import { GraphiQL, HISTORY_PLUGIN } from 'graphiql';
import { createGraphiQLFetcher } from '@graphiql/toolkit';
import 'graphiql/setup-workers/esm.sh';

const api = '/graphql';
const logo = '/graphql-ui';
const defaultQuery = '';
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

var defaultHeaders = {
	Accept: 'application/json',
	'Content-Type': 'application/json',
};

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

const fetcher = createGraphiQLFetcher({
	url: getUrl(),
	subscriptionUrl: getWsUrl(),
	headers: mergedHeaders,
});

const plugins = [HISTORY_PLUGIN];

function App() {
	return React.createElement(GraphiQL, {
		fetcher: fetcher,
		plugins: plugins,
		query: parameters.query,
		variables: parameters.variables,
		headers: parameters.headers,
		operationName: parameters.operationName,
		onEditQuery: onEditQuery,
		onEditVariables: onEditVariables,
		onEditHeaders: onEditHeaders,
		defaultEditorToolsVisibility: true,
		shouldPersistHeaders: shouldPersistHeaders,
		defaultQuery: defaultQuery,
	});
}

const container = document.getElementById('graphiql');
const root = ReactDOM.createRoot(container);
root.render(React.createElement(App));

// LOGO (top-left corner)
if (!embed) {
	// Wait for the DOM to update after React render
	setTimeout(function() {
		var sidebar = document.getElementsByClassName(
			'graphiql-sidebar'
		)[0];
		if (sidebar) {
			var logoLink = document.createElement('a');
			logoLink.id = 'graphQLUiLogoLink';
			logoLink.className = 'graphiql-un-styled';
			logoLink.href = logo;
			logoLink.innerHTML =
				"<img src='logo.png' alt='SmallRye Graphql' height='44' align='middle'>";
			sidebar.insertBefore(logoLink, sidebar.firstChild);
		}
	}, 500);
}
