#!/usr/bin/env bash
# Starts JavaQuest at http://localhost:2026. Builds first when the sources are newer than the app jar.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$ROOT/app/target/javaquest.jar"
CLASSPATH_FILE="$ROOT/practice/target/runner-classpath.txt"

if ! command -v java >/dev/null 2>&1; then
  echo "Java was not found. Install JDK 21 and try again." >&2
  exit 1
fi

if [[ ! -f "$JAR" ]] || [[ -n "$(find "$ROOT/app/src" "$ROOT/runner/src" "$ROOT/pom.xml" "$ROOT/app/pom.xml" "$ROOT/runner/pom.xml" -newer "$JAR" -print -quit)" ]]; then
  echo "Building JavaQuest..."
  "$ROOT/mvnw" -q -f "$ROOT/pom.xml" -DskipTests install
fi

if [[ ! -f "$CLASSPATH_FILE" ]] || [[ "$ROOT/practice/pom.xml" -nt "$CLASSPATH_FILE" ]]; then
  echo "Preparing the test libraries..."
  "$ROOT/mvnw" -q -f "$ROOT/practice/pom.xml" dependency:build-classpath \
    -Dmdep.outputFile="$CLASSPATH_FILE"
fi

# --unlock-all is author mode: every ticket is open, for writing and reviewing content.
ARGS=()
for arg in "$@"; do
  if [[ "$arg" == "--unlock-all" ]]; then
    ARGS+=("--javaquest.unlock-all=true")
    echo "Author mode: every ticket is open."
  else
    ARGS+=("$arg")
  fi
done

echo "JavaQuest is starting at http://localhost:2026"
exec java -jar "$JAR" --javaquest.root="$ROOT" ${ARGS[@]+"${ARGS[@]}"}
