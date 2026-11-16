#!/usr/bin/env bash
# Starts JavaQuest. Builds first when the sources are newer than the app jar.
#
#   ./run.sh               learn: http://localhost:2026, your progress in ~/.javaquest (backed up daily)
#   ./run.sh --author      build and review content: every ticket open, http://localhost:2027,
#                          a separate progress database in ~/.javaquest-author, so your learning is untouched
#   ./run.sh --unlock-all  every ticket open, but with YOUR progress database (author mode on 2026)
#
# Any other arguments go to Spring, for example --server.port=2030.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$ROOT/app/target/javaquest.jar"
CLASSPATH_FILE="$ROOT/practice/target/runner-classpath.txt"

if ! command -v java >/dev/null 2>&1; then
  echo "Java was not found. Install JDK 21 and try again." >&2
  exit 1
fi

MODE="learn"
PORT="2026"
DATA_DIR="$HOME/.javaquest"
ARGS=()
for arg in "$@"; do
  case "$arg" in
    --author)
      MODE="author"
      PORT="2027"
      DATA_DIR="$HOME/.javaquest-author"
      ARGS+=("--javaquest.unlock-all=true")
      ;;
    --unlock-all)
      MODE="unlock-all"
      ARGS+=("--javaquest.unlock-all=true")
      ;;
    --server.port=*) PORT="${arg#--server.port=}" ;;
    --javaquest.data-dir=*) DATA_DIR="${arg#--javaquest.data-dir=}" ;;
    *) ARGS+=("$arg") ;;
  esac
done

if [[ ! -f "$JAR" ]] || [[ -n "$(find "$ROOT/app/src" "$ROOT/runner/src" "$ROOT/content-model/src" "$ROOT/pom.xml" "$ROOT/content-model/pom.xml" "$ROOT/app/pom.xml" "$ROOT/runner/pom.xml" -newer "$JAR" -print -quit)" ]]; then
  echo "Building JavaQuest..."
  "$ROOT/mvnw" -q -f "$ROOT/pom.xml" -DskipTests install
fi

if [[ ! -f "$CLASSPATH_FILE" ]] || [[ "$ROOT/practice/pom.xml" -nt "$CLASSPATH_FILE" ]]; then
  echo "Preparing the test libraries..."
  "$ROOT/mvnw" -q -f "$ROOT/practice/pom.xml" dependency:build-classpath \
    -Dmdep.outputFile="$CLASSPATH_FILE"
fi

# Back up the learning progress once a day before starting, keeping the last 7 days.
# The app is not running yet, so the database file is complete.
if [[ "$MODE" != "author" && -f "$DATA_DIR/progress.mv.db" ]]; then
  BACKUPS="$DATA_DIR/backups"
  TODAY="$BACKUPS/progress-$(date +%Y-%m-%d).mv.db"
  if [[ ! -f "$TODAY" ]]; then
    mkdir -p "$BACKUPS"
    cp "$DATA_DIR/progress.mv.db" "$TODAY"
    ls -1t "$BACKUPS"/progress-*.mv.db 2>/dev/null | tail -n +8 | while read -r old; do rm -f "$old"; done
  fi
fi

# Run a copy of the jar, so a rebuild (./mvnw install) can replace app/target/javaquest.jar
# while this server keeps running. Replacing the jar under a running server makes it hang.
RUN_JAR="$ROOT/app/target/javaquest-running-$PORT.jar"
cp "$JAR" "$RUN_JAR"

case "$MODE" in
  author) echo "Author mode: every ticket is open, with a separate progress database ($DATA_DIR)." ;;
  unlock-all) echo "Every ticket is open, using your own progress database. Use --author to keep your learning untouched." ;;
esac
echo "JavaQuest is starting at http://localhost:$PORT"
exec java -jar "$RUN_JAR" --javaquest.root="$ROOT" --server.port="$PORT" --javaquest.data-dir="$DATA_DIR" ${ARGS[@]+"${ARGS[@]}"}
