# The web pool

Content and practice for the web courses (Frontend, and later Backend and Full stack in JavaScript). `courses.yaml` at the repository root decides which modules make each course.

- `content/<module>/<topic>/`: lessons and quizzes, like `content/` for Java.
- `practice/<module>/<topic>/`: `src/`, `skeleton/`, `solution/` and `test/` for each code ticket.
- `practice/package.json` and `package-lock.json`: the pinned tools (Vitest, jsdom, React, React Testing Library, esbuild). `run.sh` installs them with `npm ci`.
- `practice/tools/check.mjs` checks syntax; `practice/tools/preview.mjs` builds a page for the editor's live preview.

The format is in `docs/CONTENT_SPEC.md`, "Web courses".
