# Gamification

All rules here are pure functions in `progress/`, unit tested. Numbers are design choices to tune later, not measured values.

## The board

The story is in `docs/STORY.md`. The game layer uses its words.

- The home page is your ticket board. Each module is a project. Its tickets are the topics, then the incident (boss) and the feature (mini-project).
- Ticket states: locked (not assigned yet), to do, in progress, done (mastered), and needs review (spaced repetition is due).
- A project opens when the previous project's incident is resolved. Inside a project, a ticket is assigned when the ticket before it is done.
- "Skip ahead" assigns any locked ticket after a warning. Skipped tickets earn no mastery until completed.

## Mastery

A topic is **mastered** when both are true:

1. Best quiz score of 80% or more on one attempt.
2. The coding tests pass, from the browser or the IDE. Topics with `practice.kind: quiz` skip this condition.

Per-topic mastery percentage shown on the dashboard:

`mastery = 50 * min(quizBest / 80, 1) + 50 * testsPassedFraction`

For quiz-only topics, the quiz is the full 100.

### When a learner falls short

- Quiz under 80%: the result screen lists missed concepts. Each links to the lesson section in `reteach`. The retry draws a fresh set of variants.
- Tests failing: hints unlock one at a time. Hint 1 is available at once. Hint 2 unlocks after 2 failed submits. Hint 3 unlocks after 4.
- The solution unlocks after all 3 hints are used and 1 more failed submit, or at any time after the tests pass. Viewing it before passing sets `solution_viewed`. The topic can still be mastered, but code XP is halved.

## XP

| Event | XP |
|---|---|
| Finish reading a lesson (the Recap heading scrolls into view) | 10 |
| Quiz pass, first time | `xp.quiz` from meta (default 50) |
| Quiz 100%, first time | +20 bonus |
| Coding tests pass, first time | `xp.code` (default 100) |
| Pass with no hints | +25 |
| Boss bronze / silver / gold | 300 / +100 / +100 (not built yet: no incidents exist) |
| Mini-project complete | 400 (not built yet) |
| Review card answered correctly | 15 |
| Daily streak day | 5 times the streak length, capped at 50, once per day |

XP is a ledger (`xp_event`), never a stored total. A repeated action never awards first-time XP twice.

## Levels

~~Level `n` needs `100 * n * (n + 1) / 2` total XP.~~ Level 1 is free. Level `L` needs `100 * (L - 1) * L / 2` total XP: level 2 at 100, level 3 at 300, level 4 at 600, level 6 at 1,500. (`GameRules.xpForLevel`.) Each block of 5 levels has a job title: Intern, Junior Developer, Developer, Software Engineer, Senior Engineer, Staff Engineer, Architect, Principal Engineer. A new title shows as a promotion message from Maya.

## Streaks

- A day counts when you earn at least 10 XP. The day boundary is local midnight.
- A frozen day keeps the streak alive but does not add to it.
- One "streak freeze" is earned per 7-day streak, holding at most 2. A freeze covers one missed day automatically.

## Spaced repetition

- Each mastered topic with `review: true` gets a review card, scheduled with SM-2.
- A review is 3 questions drawn from that topic's quiz pool with a fresh seed.
- 3 of 3 correct counts as "good", 2 as "hard", and fewer as "again". "Again" resets the interval to 1 day and adds the topic to the weak list.
- The dashboard shows "Reviews due today" above "Continue".

## Weak topics

A topic is weak when either is true:

- Its last review was "again".
- Its quiz best is under 80% after 2 or more attempts.

Weak topics list their missed concepts and link straight to the reteach sections.

## Kudos (badges)

Badges appear as kudos from teammates.

| Badge | Rule |
|---|---|
| Hello, World | Master module 0 topic 5 |
| First green checks | Pass any coding test |
| No Hints Needed | Pass 10 coding exercises with no hints |
| Perfectionist | 10 quizzes at 100% |
| Bug Hunter | 20 spot-the-bug questions correct |
| IDE Native | Pass 10 exercises from the IDE |
| On Fire | 7-day streak |
| Unstoppable | 30-day streak |
| Project Shipped: N | Master every unit in module N (one per module) |
| Memory Palace | 50 good reviews |
| Java Pro | Finish the capstone (added with the capstone) |

Boss Slayer and Gold Standard come back when incidents exist. The rules live in `Kudos.java`.

## Byte, chat and feedback

- Byte is the team's owl mascot and lives in the team chat panel. Byte gives hints, explains failed checks, and cheers on done tickets.
- Moods: idle, thinking (checks running), cheering (pass), encouraging (fail). Encouraging messages never blame. Example: "Close! 4 of 5 checks pass. Want a hint?"
- Celebrations: a "ticket closed" moment on mastery, a promotion card on a new job title, and a kudos toast. All are skipped under reduced motion.
