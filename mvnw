#!/bin/sh

set -eu

BASE_DIR=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd)
WRAPPER_DIR="$BASE_DIR/.mvn/wrapper"
WRAPPER_JAR="$WRAPPER_DIR/maven-wrapper.jar"
PROPERTIES_FILE="$WRAPPER_DIR/maven-wrapper.properties"

if [ ! -f "$PROPERTIES_FILE" ]; then
  echo "Missing $PROPERTIES_FILE" >&2
  exit 1
fi

read_property() {
  key="$1"
  sed -n "s/^${key}=//p" "$PROPERTIES_FILE" | tail -n 1
}

WRAPPER_URL=$(read_property wrapperUrl)

if [ -z "$WRAPPER_URL" ]; then
  echo "Missing wrapperUrl in $PROPERTIES_FILE" >&2
  exit 1
fi

if [ ! -f "$WRAPPER_JAR" ]; then
  if command -v curl >/dev/null 2>&1; then
    mkdir -p "$WRAPPER_DIR"
    curl -fsSL "$WRAPPER_URL" -o "$WRAPPER_JAR"
  else
    echo "curl is required to download Maven Wrapper." >&2
    exit 1
  fi
fi

exec java "-Dmaven.multiModuleProjectDirectory=$BASE_DIR" -classpath "$WRAPPER_JAR" org.apache.maven.wrapper.MavenWrapperMain "$@"