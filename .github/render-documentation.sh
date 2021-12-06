#!/bin/bash
set -euo pipefail
IFS=$'\n\t'

SR_GRAPHQL_VERSION=$(cat .github/project.yml | yq eval '.release.current-version' -)  && mike deploy --push --update-aliases $SR_GRAPHQL_VERSION latest --branch gh-pages
SR_GRAPHQL_VERSION=$(cat .github/project.yml | yq eval '.release.current-version' -)  && mike set-default --push latest
