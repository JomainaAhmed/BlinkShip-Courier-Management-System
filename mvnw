#!/usr/bin/env bash
set -e
DIR=$(cd "$(dirname "$0")" && pwd)
java -jar "$DIR/.mvn/wrapper/maven-wrapper.jar" "$@"
