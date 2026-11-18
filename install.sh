#!/usr/bin/env bash
# One command to get Cadence and start it: clones it (or updates your copy), checks this laptop, builds, starts,
# and opens it in your browser.
#
#   bash <(curl -fsSL https://raw.githubusercontent.com/sahilbeg/JavaQuest/main/install.sh)   (public repo)
#   ./install.sh                                                                                 (from a copy of this file)
#
# Where it goes: ~/Cadence, or the folder in CADENCE_DIR. Any arguments go to run.sh, for example --author.
set -euo pipefail

REPO_URL="${CADENCE_REPO:-https://github.com/sahilbeg/JavaQuest.git}"
DEST="${CADENCE_DIR:-$HOME/Cadence}"

if [[ -t 1 ]]; then GREEN=$'\033[32m'; RED=$'\033[31m'; BOLD=$'\033[1m'; RESET=$'\033[0m'; else GREEN=""; RED=""; BOLD=""; RESET=""; fi
ok()   { printf '  %s✓%s %s\n' "$GREEN" "$RESET" "$1"; }
fail() { printf '  %s✗%s %s\n' "$RED" "$RESET" "$1" >&2; shift; for line in "$@"; do printf '    %s\n' "$line" >&2; done; exit 1; }

echo "${BOLD}Cadence${RESET}: getting the course"

if ! command -v git >/dev/null 2>&1 || ! git --version >/dev/null 2>&1; then
  fail "Git is not installed." \
       "On a Mac, run:  xcode-select --install" \
       "Then run this command again."
fi
ok "$(git --version)"

if [[ -d "$DEST/.git" ]]; then
  ok "Found your copy in $DEST"
  # Only a fast-forward: your own code in practice/*/src is never touched, and nothing is ever reset.
  if git -C "$DEST" pull --ff-only --quiet 2>/dev/null; then
    ok "Updated to the latest course"
  else
    echo "  Couldn't update it (offline, or local changes in the way). Starting the version you have."
  fi
elif [[ -e "$DEST" ]]; then
  fail "$DEST already exists, but it isn't a Cadence copy." \
       "Move it away, or choose another folder:  CADENCE_DIR=~/Code/Cadence ./install.sh"
else
  echo "  Downloading Cadence into $DEST..."
  git clone --quiet "$REPO_URL" "$DEST" \
    || fail "Couldn't download Cadence from $REPO_URL." \
            "Check your internet connection." \
            "If the repository is private, ask its owner for access, then sign in to GitHub (for example: gh auth login)."
  ok "Downloaded into $DEST"
fi

cd "$DEST"
# When this script was piped in, its input is not the keyboard: give run.sh the terminal back for its questions.
if [[ ! -t 0 ]] && { : </dev/tty; } 2>/dev/null; then exec ./run.sh "$@" </dev/tty; fi
exec ./run.sh "$@"
