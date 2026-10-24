#!/usr/bin/env sh
# NOVA bootstrap wrapper: downloads the pinned official Gradle distribution on first run.
set -eu
GRADLE_VERSION="8.8"
GRADLE_USER_HOME="${GRADLE_USER_HOME:-$HOME/.gradle}"
BASE="$GRADLE_USER_HOME/nova-bootstrap"
DIST="$BASE/gradle-$GRADLE_VERSION"
ZIP="$BASE/gradle-$GRADLE_VERSION-bin.zip"
if [ ! -x "$DIST/bin/gradle" ]; then
  mkdir -p "$BASE"
  if [ ! -f "$ZIP" ]; then
    echo "NOVA: downloading Gradle $GRADLE_VERSION (first run only)..."
    if command -v curl >/dev/null 2>&1; then
      curl -fL --retry 3 "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip" -o "$ZIP"
    elif command -v wget >/dev/null 2>&1; then
      wget -O "$ZIP" "https://services.gradle.org/distributions/gradle-$GRADLE_VERSION-bin.zip"
    else
      echo "Error: curl or wget is required to bootstrap Gradle." >&2
      exit 1
    fi
  fi
  echo "NOVA: extracting Gradle $GRADLE_VERSION..."
  unzip -q -o "$ZIP" -d "$BASE"
fi
exec "$DIST/bin/gradle" "$@"
