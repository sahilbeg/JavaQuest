---
description: Report where the Cadence build stopped, then continue with the next milestone
argument-hint: "[optional: a milestone or task to start from, e.g. C09]"
---

Continue building Cadence from where it stopped.

1. Read `CLAUDE.md`, then `build_state.json`: `milestone`, `nextTask`, `resumeHere`, `blockers`, `openDecisions`. Check `git status` and `git log --oneline -5` for uncommitted or half-finished work.
2. Print a short status for the user:
   - the last finished milestone and the one in progress;
   - per module, how many units are `planned`, `ui_wired` and `done`;
   - the next task, any blockers, and anything uncommitted.
   Modified files under `practice/*/*/src/` are the user's own learning code: never touch, stage or revert them (see "The user is also learning with this app" in CLAUDE.md).
3. If `$ARGUMENTS` names a milestone or task, start there. Otherwise start at `nextTask`. If the working tree has uncommitted changes you did not expect, stop and ask the user first.
4. Build the milestone exactly as `CLAUDE.md` describes, under "How a content module is built" (or the ROADMAP's checklist for M5 and M6). Skip units whose files exist and whose status is past `planned`.
5. Finish as `CLAUDE.md` describes, under "Finishing a milestone": all checks green, review findings fixed, state files updated, then commit.
