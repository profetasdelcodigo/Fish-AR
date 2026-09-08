#!/bin/sh

APP_HOME=$(CDPATH= cd -- "$(dirname -- "$0")" && pwd -P)

if [ -n "$JAVA_HOME" ] && [ -x "$JAVA_HOME/bin/java" ]; then
  JAVACMD="$JAVA_HOME/bin/java"
else
  JAVACMD="java"
fi

if ! command -v "$JAVACMD" >/dev/null 2>&1; then
  echo "ERROR: Java was not found. Set JAVA_HOME or install Java." >&2
  exit 1
fi

GRADLE_VERSION="9.3.1"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
DIST_DIR="$GRADLE_USER_HOME/wrapper/dists/gradle-$GRADLE_VERSION-bin"
ZIP="$DIST_DIR/gradle-$GRADLE_VERSION-bin.zip"
INSTALL_DIR="$DIST_DIR/gradle-$GRADLE_VERSION"

if [ ! -x "$INSTALL_DIR/bin/gradle" ]; then
  mkdir -p "$DIST_DIR"
  if [ ! -f "$ZIP" ]; then
    echo "Downloading Gradle $GRADLE_VERSION..."
    curl -fL --retry 3 --connect-timeout 20 "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP" || exit 1
  fi
  rm -rf "$INSTALL_DIR.tmp"
  mkdir -p "$INSTALL_DIR.tmp"
  unzip -q "$ZIP" -d "$INSTALL_DIR.tmp" || exit 1
  mv "$INSTALL_DIR.tmp/gradle-$GRADLE_VERSION" "$INSTALL_DIR"
  rm -rf "$INSTALL_DIR.tmp"
fi

exec "$INSTALL_DIR/bin/gradle" "$@"
