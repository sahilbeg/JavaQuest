#!/usr/bin/env bash
# Checks one practice unit quickly, the way Cadence's runner does.
# Usage: .claude/scripts/check-topic.sh <practice unit dir> [solution|skeleton|src]   (default: both solution and skeleton)
# Compiles <dir>/<variant> and <dir>/test against practice/target/runner-classpath.txt with -parameters,
# runs JUnit with the runner's JVM flags, and prints each test with its result.
# Expect: solution passes every test; skeleton fails, with assertion messages and no compile errors.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/../.." && pwd)"
UNIT="$(cd "${1:?Usage: check-topic.sh <practice unit dir> [solution|skeleton|src]}" && pwd)"
VARIANTS=("${2:-solution}")
[[ $# -lt 2 ]] && VARIANTS=(solution skeleton)
CP_FILE="$ROOT/practice/target/runner-classpath.txt"

if [[ ! -f "$CP_FILE" || "$ROOT/practice/pom.xml" -nt "$CP_FILE" ]]; then
  echo "Refreshing the runner classpath..."
  "$ROOT/mvnw" -q -f "$ROOT/practice/pom.xml" dependency:build-classpath -Dmdep.outputFile="$CP_FILE"
fi
CP="$(cat "$CP_FILE")"
MOCKITO="$(tr ':' '\n' < "$CP_FILE" | grep 'mockito-core-' | head -1)"

# Spring units get the larger heap, as in TopicMeta.Practice.
HEAP=256
META="$ROOT/content/${UNIT#"$ROOT/practice/"}/meta.yaml"
if [[ -f "$META" ]] && grep -q 'runner: *spring' "$META"; then HEAP=512; fi

for VARIANT in "${VARIANTS[@]}"; do
  WORK="$(mktemp -d)"
  trap 'rm -rf "$WORK"' EXIT
  echo "== $(basename "$UNIT") / $VARIANT (heap ${HEAP} MB)"
  if [[ -d "$UNIT/$VARIANT" ]] && [[ -d "$UNIT/test" ]]; then
    javac --release 21 -proc:none -parameters -encoding UTF-8 -nowarn -d "$WORK/main" -cp "$CP" $(find "$UNIT/$VARIANT" -name '*.java')
    javac --release 21 -proc:none -parameters -encoding UTF-8 -nowarn -d "$WORK/test" -cp "$CP:$WORK/main" $(find "$UNIT/test" -name '*.java')
  else
    echo "missing $UNIT/$VARIANT or $UNIT/test" >&2; exit 2
  fi
  START=$(date +%s)
  (cd "$WORK" && java -Xmx${HEAP}m -Xss1m -XX:+UseSerialGC -XX:TieredStopAtLevel=1 -Djava.io.tmpdir="$WORK" \
      -Duser.language=en -Duser.country=US -javaagent:"$MOCKITO" -Xshare:off \
      -cp "$CP:$WORK/main:$WORK/test" org.junit.platform.console.ConsoleLauncher execute \
      --scan-classpath "$WORK/test" --disable-banner --details=tree --disable-ansi-colors 2>&1 || true) \
    | grep -E '(✔|✘)' | grep -vE 'JUnit (Jupiter|Vintage|Platform Suite)' | sed -E 's/ ==> .*//' || true
  echo "   took $(( $(date +%s) - START )) s"
  rm -rf "$WORK"
  trap - EXIT
done
