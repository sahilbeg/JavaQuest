# Gamification

All rules here are pure functions in `progress/`, unit tested. Numbers are design choices to tune later, not measured values.

## World map

- Each module is a world on the map (names in CURRICULUM.md). Topics are stops along a path.
- States: locked (grey padlock), available (glowing), in progress (half ring), mastered (gold star), needs review (small clock).
- A world unlocks when the previous world's boss is beaten. Inside a world, a stop unlocks when the previous stop is mastered.
- A "Skip ahead" option unlocks any topic after a warning. Skipped topics earn no mastery until completed. This keeps an experienced learner from being stuck.

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
| Finish reading a lesson (scrolled to Recap) | 10 |
| Quiz pass, first time | `xp.quiz` from meta (default 50) |
| Quiz 100%, first time | +20 bonus |
| Coding tests pass, first time | `xp.code` (default 100) |
| Pass with no hints | +25 |
| Boss bronze / silver / gold | 300 / +100 / +100 |
| Mini-project complete | 400 |
| Review card answered correctly | 15 |
| Daily streak day | 5 times the streak length, capped at 50 |

XP is a ledger (`xp_event`), never a stored total. A repeated action never awards first-time XP twice.

## Levels

Level `n` needs `100 * n * (n + 1) / 2` total XP: 100, 300, 600, 1000, and so on. Titles every 5 levels: Novice, Apprentice, Coder, Developer, Engineer, Senior Engineer, Architect, Java Pro.

## Streaks

- A day counts when you earn at least 10 XP. The day boundary is local midnight.
- One "streak freeze" is earned per 7-day streak, holding at most 2. A freeze covers one missed day automatically.

## Spaced repetition

- Each mastered topic with `review: true` gets a review card, scheduled with SM-2.
- A review is 3 questions drawn from that topic's quiz pool, avoiding the last attempt's questions.
- 3 of 3 correct counts as "good", 2 as "hard", and fewer as "again". "Again" resets the interval to 1 day and adds the topic to the weak list.
- The dashboard shows "Reviews due today" above "Continue".

## Weak topics

A topic is weak when either is true:

- Its last review was "again".
- Its quiz best is under 80% after 2 or more attempts.

Weak topics list their missed concepts and link straight to the reteach sections.

## Badges

| Badge | Rule |
|---|---|
| Hello, World | Master module 0 topic 5 |
| First Blood | Pass any coding test |
| No Hints Needed | Pass 10 coding exercises with no hints |
| Perfectionist | 10 quizzes at 100% |
| Bug Hunter | 20 spot-the-bug questions correct |
| IDE Native | Pass 10 exercises from the IDE |
| On Fire | 7-day streak |
| Unstoppable | 30-day streak |
| Boss Slayer | Beat any boss |
| Gold Standard | Gold tier on any boss |
| World Clear: N | Master every unit in module N (one badge per module) |
| Memory Palace | 50 good reviews |
| Java Pro | Finish the capstone |

## Mascot and feedback

- Mascot: **Byte**, a small owl. Byte appears in lesson callouts, on results, and on the empty dashboard.
- Moods: idle, thinking (test run in progress), cheering (pass), encouraging (fail). Encouraging messages never blame. Example: "Close! 4 of 5 tests pass. Want hint 1?"
- Celebrations: confetti on mastery, a level-up card, and a badge toast. All are skipped under reduced motion.
