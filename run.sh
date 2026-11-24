#!/usr/bin/env bash
# Starts Cadence: checks this laptop, builds when the sources are newer than the app jar, then starts the app
# and opens it in your browser.
#
#   ./run.sh               learn: http://localhost:2026, your progress in ~/.cadence (backed up daily);
#                          an existing ~/.javaquest from before the rename is used as it is
#   ./run.sh --check       only check this laptop (Java, Node, port, disk, internet) and say what to fix
#   ./run.sh --no-browser  start without opening the browser
#   ./run.sh --author      build and review content: every ticket open, http://localhost:2027,
#                          a separate progress database in ~/.cadence-author (or an existing ~/.javaquest-author),
#                          so your learning is untouched
#   ./run.sh --unlock-all  every ticket open, but with YOUR progress database (author mode on 2026)
#   ./run.sh --tester      a tester: every ticket of every course open, http://localhost:3004, its own progress in
#                          ~/.cadence-tester, so test runs never touch a learner's progress
#
# Any other arguments go to Spring, for example --server.port=2030. The old --javaquest.* names still work.
set -euo pipefail

ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")" && pwd)"
JAR="$ROOT/app/target/cadence.jar"
CLASSPATH_FILE="$ROOT/practice/target/runner-classpath.txt"

# ---------- Output ----------
if [[ -t 1 ]]; then GREEN=$'\033[32m'; YELLOW=$'\033[33m'; RED=$'\033[31m'; BOLD=$'\033[1m'; RESET=$'\033[0m'
else GREEN=""; YELLOW=""; RED=""; BOLD=""; RESET=""; fi
ok()   { printf '  %s✓%s %s\n' "$GREEN" "$RESET" "$1"; }
warn() { printf '  %s!%s %s\n' "$YELLOW" "$RESET" "$1" >&2; shift; for line in "$@"; do printf '    %s\n' "$line" >&2; done; }
# Prints what is wrong and how to fix it, then stops.
fail() { printf '  %s✗%s %s\n' "$RED" "$RESET" "$1" >&2; shift; for line in "$@"; do printf '    %s\n' "$line" >&2; done; exit 1; }
interactive() { [[ -t 0 && -t 1 ]]; }

# ---------- Arguments ----------
MODE="learn"
PORT="2026"
# The project was called JavaQuest before. Progress saved under the old folder name keeps working, untouched:
# use the new folder when it exists, else the old one when it exists, else start the new one.
progress_dir() { if [[ -d "$HOME/$1" ]]; then echo "$HOME/$1"; elif [[ -d "$HOME/$2" ]]; then echo "$HOME/$2"; else echo "$HOME/$1"; fi; }
DATA_DIR="$(progress_dir .cadence .javaquest)"
CHECK_ONLY=false
OPEN_BROWSER=true
ARGS=()
for arg in "$@"; do
  arg="${arg/#--javaquest./--cadence.}"
  case "$arg" in
    --author)
      MODE="author"
      PORT="2027"
      DATA_DIR="$(progress_dir .cadence-author .javaquest-author)"
      ARGS+=("--cadence.unlock-all=true")
      ;;
    --tester)
      MODE="tester"
      PORT="3004"
      DATA_DIR="$HOME/.cadence-tester"
      ARGS+=("--cadence.unlock-all=true")
      ;;
    --unlock-all)
      MODE="unlock-all"
      ARGS+=("--cadence.unlock-all=true")
      ;;
    --check) CHECK_ONLY=true ;;
    --no-browser) OPEN_BROWSER=false ;;
    --server.port=*) PORT="${arg#--server.port=}" ;;
    --cadence.data-dir=*) DATA_DIR="${arg#--cadence.data-dir=}" ;;
    *) ARGS+=("$arg") ;;
  esac
done
URL="http://localhost:$PORT"
# A browser opens only for a person at a terminal on a Mac, never for a script or a background start.
if ! interactive || ! command -v open >/dev/null 2>&1; then OPEN_BROWSER=false; fi

open_when_ready() {
  [[ "$OPEN_BROWSER" == true ]] || return 0
  ( for _ in $(seq 1 180); do
      if curl -fs -o /dev/null "$URL/" 2>/dev/null; then open "$URL"; exit 0; fi
      sleep 1
    done ) >/dev/null 2>&1 &
}

echo "${BOLD}Cadence${RESET}: checking this laptop"

# ---------- 1. Operating system ----------
case "$(uname -s)" in
  Darwin) ok "macOS $(sw_vers -productVersion 2>/dev/null || true)" ;;
  Linux)  warn "Linux works in principle but is untested. Cadence is made for macOS." ;;
  *)      fail "This looks like Windows ($(uname -s)). Cadence doesn't support Windows yet." \
               "Use a Mac, or try Linux inside WSL (untested)." ;;
esac

# ---------- 2. Java 21 or newer ----------
# The major version of a java binary: "21.0.5" → 21, "25" → 25, old "1.8.0_402" → 1 (Java 8 or older).
java_major() { "$1" -version 2>&1 | head -n 1 | sed -E 's/[^"]*"([0-9]+).*/\1/'; }
java_ok() { local major; major="$(java_major "$1" 2>/dev/null || true)"; [[ "$major" =~ ^[0-9]+$ ]] && (( major >= 21 )); }
JAVA_BIN=""
if command -v java >/dev/null 2>&1 && java_ok "$(command -v java)"; then
  JAVA_BIN="$(command -v java)"
elif [[ -x /usr/libexec/java_home ]] && FOUND="$(/usr/libexec/java_home -v 21+ 2>/dev/null)" && java_ok "$FOUND/bin/java"; then
  # A JDK 21+ is installed, just not the default: use it for Cadence.
  JAVA_BIN="$FOUND/bin/java"
  warn "Your default 'java' is older than 21, so Cadence uses the JDK 21+ it found at $FOUND." \
       "To make it your default too, add this line to ~/.zshrc:  export JAVA_HOME=\"\$(/usr/libexec/java_home -v 21)\""
fi
if [[ -z "$JAVA_BIN" ]] && command -v brew >/dev/null 2>&1 && interactive && [[ "$CHECK_ONLY" == false ]]; then
  echo "  Cadence needs Java 21 or newer, and this laptop doesn't have it."
  read -r -p "  Install it now with Homebrew (brew install --cask temurin@21)? [Y/n] " answer || answer="n"
  if [[ -z "$answer" || "$answer" =~ ^[Yy] ]]; then
    brew install --cask temurin@21 || fail "Homebrew couldn't install Java." "Install Temurin 21 from https://adoptium.net, then run ./run.sh again."
    if FOUND="$(/usr/libexec/java_home -v 21+ 2>/dev/null)" && java_ok "$FOUND/bin/java"; then JAVA_BIN="$FOUND/bin/java"; fi
  fi
fi
if [[ -z "$JAVA_BIN" ]]; then
  if command -v java >/dev/null 2>&1; then FOUND_TEXT="'java' here is: $(java -version 2>&1 | head -n 1)"; else FOUND_TEXT="no Java was found"; fi
  fail "Cadence needs Java 21 or newer, but $FOUND_TEXT." \
       "Install it with Homebrew:  brew install --cask temurin@21" \
       "or download Temurin 21 from https://adoptium.net. Then run ./run.sh again."
fi
# The build tool (mvnw) reads JAVA_HOME before PATH, so point both at the chosen Java.
JAVA_HOME="$("$JAVA_BIN" -XshowSettings:properties -version 2>&1 | sed -n -E 's/^ *java\.home = (.*)$/\1/p' | head -n 1)"
export JAVA_HOME
export PATH="$JAVA_HOME/bin:$PATH"
ok "Java $("$JAVA_BIN" -version 2>&1 | head -n 1 | sed -E 's/[^"]*"([^"]+)".*/\1/')"

# ---------- 3. The port ----------
if command -v lsof >/dev/null 2>&1 && lsof -tiTCP:"$PORT" -sTCP:LISTEN >/dev/null 2>&1; then
  if curl -fs -o /dev/null "$URL/api/catalog" 2>/dev/null; then
    ok "Cadence is already running at $URL"
    if [[ "$CHECK_ONLY" == false ]]; then
      [[ "$OPEN_BROWSER" == true ]] && open "$URL"
      echo "Nothing to start: open $URL. To run a second copy, use another port, for example: ./run.sh --server.port=$((PORT + 4))"
      exit 0
    fi
  else
    fail "Port $PORT is used by another program." "Start Cadence on another port, for example: ./run.sh --server.port=$((PORT + 4))"
  fi
else
  ok "Port $PORT is free"
fi

# ---------- 4. First start: disk space and internet ----------
NEEDS_DOWNLOADS=false
if [[ ! -f "$JAR" || ! -f "$CLASSPATH_FILE" ]]; then NEEDS_DOWNLOADS=true; fi
if [[ "$NEEDS_DOWNLOADS" == true ]]; then
  FREE_MB=$(( $(df -Pk "$ROOT" | awk 'NR==2 {print $4}') / 1024 ))
  if (( FREE_MB < 1024 )); then
    warn "Only $FREE_MB MB of disk space is free. The first start downloads and builds about 300 MB." "Free up some space if the build fails."
  else
    ok "Disk space: $FREE_MB MB free"
  fi
  if curl -fsI --max-time 8 https://repo.maven.apache.org/maven2/ >/dev/null 2>&1; then
    ok "Internet: the library server answers"
  else
    warn "Can't reach the library server (repo.maven.apache.org)." \
         "The first start downloads the build tool and libraries. If the build fails, check your connection or proxy."
  fi
fi

# ---------- 5. Node, for the web courses ----------
# The web courses (HTML, CSS, JavaScript, React) check practice code with Node. The Java course doesn't need it,
# so a missing or old Node is a warning, not a stop.
WEB_PRACTICE="$ROOT/tracks/web/practice"
node_major() { node --version 2>/dev/null | sed -E 's/^v([0-9]+).*/\1/'; }
NODE_READY=false
if command -v node >/dev/null 2>&1 && [[ "$(node_major)" =~ ^[0-9]+$ ]] && (( $(node_major) >= 20 )); then
  NODE_READY=true
  ok "Node $(node --version) for the web courses"
else
  warn "Node 20 or newer isn't installed, so the web courses (Frontend, Full stack) can't check your code yet." \
       "The Java course works without it. To add it: brew install node, then start Cadence again."
fi

# ---------- 6. The commit guard ----------
# In a git clone, switch on the guard that stops commits from changing a ticket's src/ (the learner's own code).
if [[ -d "$ROOT/.git" ]] && command -v git >/dev/null 2>&1 && ! git -C "$ROOT" config --get core.hooksPath >/dev/null 2>&1; then
  git -C "$ROOT" config core.hooksPath .githooks 2>/dev/null || true
fi

if [[ "$CHECK_ONLY" == true ]]; then
  echo "${GREEN}Everything looks ready.${RESET} Start Cadence with: ./run.sh"
  exit 0
fi

# ---------- Build ----------
if [[ ! -f "$JAR" ]] || [[ -n "$(find "$ROOT/app/src" "$ROOT/runner/src" "$ROOT/content-model/src" "$ROOT/pom.xml" "$ROOT/content-model/pom.xml" "$ROOT/app/pom.xml" "$ROOT/runner/pom.xml" -newer "$JAR" -print -quit)" ]]; then
  if [[ -f "$JAR" ]]; then echo "Building Cadence..."; else echo "Building Cadence for the first time. This takes a few minutes..."; fi
  "$ROOT/mvnw" -q -f "$ROOT/pom.xml" -DskipTests install \
    || fail "The build failed. The messages above say why." \
            "Most often: no internet on the first start, or a company proxy. See README.md, Troubleshooting."
fi

if [[ ! -f "$CLASSPATH_FILE" ]] || [[ "$ROOT/practice/pom.xml" -nt "$CLASSPATH_FILE" ]]; then
  echo "Preparing the test libraries..."
  "$ROOT/mvnw" -q -f "$ROOT/practice/pom.xml" dependency:build-classpath -Dmdep.outputFile="$CLASSPATH_FILE" \
    || fail "Preparing the test libraries failed. The messages above say why." \
            "Most often: no internet on the first start. See README.md, Troubleshooting."
fi

# The web courses' test tools (Vitest, React Testing Library, esbuild), pinned in package-lock.json.
if [[ "$NODE_READY" == true && -f "$WEB_PRACTICE/package-lock.json" ]] \
   && { [[ ! -d "$WEB_PRACTICE/node_modules" ]] || [[ "$WEB_PRACTICE/package-lock.json" -nt "$WEB_PRACTICE/node_modules/.package-lock.json" ]]; }; then
  echo "Preparing the web course tools..."
  (cd "$WEB_PRACTICE" && npm ci --no-audit --no-fund --loglevel=error) \
    || warn "Installing the web course tools failed. The web courses can't check code until it works." \
            "Most often: no internet on the first start. Start Cadence again to retry."
fi

# ---------- Start ----------
# Back up the learning progress once a day before starting, keeping the last 7 days.
# The app is not running yet, so the database file is complete.
if [[ "$MODE" != "author" && "$MODE" != "tester" && -f "$DATA_DIR/progress.mv.db" ]]; then
  BACKUPS="$DATA_DIR/backups"
  TODAY="$BACKUPS/progress-$(date +%Y-%m-%d).mv.db"
  if [[ ! -f "$TODAY" ]]; then
    mkdir -p "$BACKUPS"
    cp "$DATA_DIR/progress.mv.db" "$TODAY"
    ls -1t "$BACKUPS"/progress-*.mv.db 2>/dev/null | tail -n +8 | while read -r old; do rm -f "$old"; done
  fi
fi

# Run a copy of the jar, so a rebuild (./mvnw install) can replace app/target/cadence.jar
# while this server keeps running. Replacing the jar under a running server makes it hang.
RUN_JAR="$ROOT/app/target/cadence-running-$PORT.jar"
cp "$JAR" "$RUN_JAR"

case "$MODE" in
  author) echo "Author mode: every ticket is open, with a separate progress database ($DATA_DIR)." ;;
  tester) echo "Tester mode: every ticket of every course is open, with the tester's own progress database ($DATA_DIR)." ;;
  unlock-all) echo "Every ticket is open, using your own progress database. Use --author to keep your learning untouched." ;;
esac
if [[ "$OPEN_BROWSER" == true ]]; then WHEN=" It opens in your browser when it's ready."; else WHEN=""; fi
echo "Cadence is starting at ${BOLD}$URL${RESET}.$WHEN Press Ctrl+C to stop."
open_when_ready
exec "$JAVA_BIN" -jar "$RUN_JAR" --cadence.root="$ROOT" --server.port="$PORT" --cadence.data-dir="$DATA_DIR" ${ARGS[@]+"${ARGS[@]}"}
