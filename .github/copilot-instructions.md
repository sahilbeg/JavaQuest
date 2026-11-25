# Copilot instructions

Read [AGENTS.md](../AGENTS.md) first. It holds the rules for every AI assistant in this repository. The short version:

- The person you're helping is also a learner in this app. Their progress (`~/.cadence` or `~/.javaquest`) and their ticket code (every existing `src/` under `practice/` and `tracks/*/practice/`) must never be changed, reset or committed.
- Test and preview with `./run.sh --tester` (http://localhost:3004), which has its own progress and code. Never use the learner app on port 2026 for testing, and never use `--unlock-all`.
- Fix starting code in `skeleton/` and answers in `solution/`. Stage files by path. Never bypass the pre-commit hook.
- Before a pull request, run `./mvnw -q install`, then the `check-content` and `verify-practice` checks listed in AGENTS.md.
