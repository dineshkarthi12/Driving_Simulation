#!/usr/bin/env bash
# Builds the application with the Maven Wrapper and launches the CLI.
# Requires only `java` (21+) on PATH; the wrapper downloads Maven on first use.
# No state is persisted between runs: every invocation starts with an empty field.
set -euo pipefail

cd "$(dirname "${BASH_SOURCE[0]}")"

# Build quietly, skipping tests for a fast start. Build output goes to stderr
# so that stdout carries only the application's own output.
./mvnw -q -B -Dmaven.test.skip=true package 1>&2

exec java -jar target/driving-simulation.jar
