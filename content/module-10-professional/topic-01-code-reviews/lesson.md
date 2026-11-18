## Hook

Before a newspaper prints a story, an editor reads it. The editor catches a wrong date, a confusing sentence, a claim with no source. The writer is not a bad writer. Two pairs of eyes simply see more than one.

Software teams do the same with code. Nothing reaches `main` until a teammate has read it. From this week you are one of those readers. A teammate's pull request is waiting for you, and your own came back with 23 comments. In this ticket you learn to give a review that helps, and to take one without flinching.

## Concept

### Why teams review code

```callout jargon
A **code review** is a teammate reading your change before it is merged, and leaving comments. The person who wrote the change is the **author**. The person reading it is the **reviewer**.
```

Reviews earn their time in four ways:

1. **Bugs are caught early.** A bug found in review costs a comment. The same bug found in production costs an incident, a refund and a late night.
2. **Knowledge spreads.** At least two people now understand every line. If the author goes on holiday, the code is not a mystery.
3. **The code stays consistent.** The same problems get solved the same way, so the codebase reads like one team wrote it.
4. **People learn.** Juniors learn from seniors' comments. Seniors learn new tricks from juniors' code. Both happen in every review.

Big tech firms require at least one approval on every change to `main`. The idea is not "we don't trust you". It is "nobody is perfect, so we check each other".

### What a review is not

A review is not an exam, and the reviewer is not a judge. Three things a review should never become:

- **A stage to show off.** Fifty comments to prove how much you know slow the team and teach nothing.
- **A gate kept shut.** Holding a PR until it is written exactly the way you would write it. Different is not wrong.
- **A verdict on the person.** The review is about this change, on this day. It says nothing about how clever the author is.

The test for a good review: the code is better, the author learned something, and the change merged without needless waiting. Google's public review guide gives a similar rule. Approve once a change makes the code healthier overall, even if it is not perfect.

### The life of a pull request

You met pull requests in [the Git workflow ticket](/t/module-07-tooling/topic-03-git-workflow), and their checks in [continuous integration](/t/module-07-tooling/topic-04-ci-basics). Here is the full journey, from push to merge.

```callout jargon
A **draft PR** is a pull request marked "not ready yet". Teammates can look early, but nobody is asked to review. **LGTM** means "looks good to me", the classic approval comment.
```

```animate
title: A pull request's journey to main
kind: scene
nodes:
  - { id: author, label: Author, emoji: "🧑‍💻", row: 0, col: 0 }
  - { id: pr, label: "PR #482", emoji: "📝", value: draft, row: 0, col: 2 }
  - { id: ci, label: CI checks, emoji: "🤖", value: idle, row: 1, col: 3 }
  - { id: reviewer, label: Reviewer, emoji: "🔍", row: 0, col: 4 }
  - { id: main, label: main, emoji: "🌳", row: 2, col: 2 }
steps:
  - say: The author pushes a branch and opens a PR. It is a draft until they have reviewed their own diff.
    send: { from: author, to: pr, label: "git push" }
  - say: Every push starts CI. It builds the code and runs every test.
    send: { from: pr, to: ci, label: "build + test" }
    set: { ci: running }
  - say: One test fails. The author fixes it before asking anyone to read. Reviewers' time is precious.
    send: [{ from: ci, to: pr, label: "red", back: true }, { from: author, to: pr, label: "fix pushed" }]
    set: { ci: green, pr: ready for review }
  - say: The reviewer reads the diff and asks for two changes, one of them a real bug.
    send: { from: reviewer, to: pr, label: "request changes" }
    set: { pr: changes requested }
  - say: The author pushes fixes and replies to every comment. CI runs again.
    send: [{ from: author, to: pr, label: "fixes + replies" }, { from: pr, to: ci, label: "build + test" }]
    set: { ci: green }
  - say: The reviewer checks the fixes and approves.
    send: { from: reviewer, to: pr, label: "approve" }
    set: { pr: approved }
  - say: Green checks plus an approval let the PR merge. The branch is deleted.
    send: { from: pr, to: main, label: "merge" }
    set: { pr: merged }
    focus: [main]
```

Branch protection, from the CI ticket, enforces the rules: no merge while checks are red or approvals are missing.

### Writing a reviewable PR

Reviewing starts with the author. A reviewer can read 200 to 400 lines with care in about an hour. Past that, attention fades and bugs slip through. So a good PR is:

- **Small.** Under about 400 changed lines, ideally much less. Split big work into a chain of PRs that each still build and pass. These are called **stacked PRs**.
- **About one thing.** One bug fix, or one feature slice, or one refactor. Never a refactor mixed with a behaviour change: the reviewer cannot tell which lines change what the code does.
- **Green before review.** CI passes before you request anyone.

```callout tip
Gojo says: if a rename touches 60 files, send it as its own PR first. A pure rename is quick to approve. Then your real change is 40 lines, not 4,000.
```

### A description that answers the questions

The description is the reviewer's map. It answers three questions before anyone has to ask them: **what** changed, **why**, and **how you tested it**.

```text
Add ride history endpoint (SPK-1012)

What: GET /riders/{riderId}/rentals?since=2026-11-01 returns the rider's
past rides, newest first, with the fare of each.

Why: riders keep emailing support to ask what they were charged.
Support answers about 40 of these a day.

How tested: unit tests for the mapping, a MockMvc test for the endpoint,
and by hand against the local database:

  $ curl -s localhost:8080/riders/rider-7/rentals?since=2026-11-01
  [{"id":"rental-17","bikeId":"B-1042","minutes":20,"fareCents":700}]

Not in this PR: paging. That comes in SPK-1013.
```

Real output, like the `curl` line, shows the code works. For a screen change, a before-and-after screenshot does the same job. "Not in this PR" stops reviewers asking for work you planned anyway.

### Review your own PR first

Before you click "Ready for review", read your own diff in the PR page, top to bottom, as if a stranger wrote it. The diff looks different from your editor, and mistakes jump out.

A self-review checklist:

- Leftover debug lines, such as `System.out.println("HERE")`, commented-out code, or a `TODO` with no ticket.
- Files you did not mean to change, such as a reformatted file or an IDE settings file.
- A test for each new behaviour, and a test for the bug you fixed.
- The description still matches the code after your last change.

Self-review catches the cheap problems, so the reviewer can spend their time on the hard ones.

### What to look for, in priority order

Reviewer time is limited, so spend it where a mistake costs most. Check in this order:

1. **Correctness and edge cases.** Does it do what the description says, for every input?
2. **Design and responsibilities.** Is each piece in the right class and layer?
3. **Tests.** Would they fail if the change were wrong?
4. **Security.** Injection, secrets, who is allowed to see what.
5. **Concurrency.** Two requests at once, shared state.
6. **Performance.** Queries in loops, work in hot paths (code that runs on every request).
7. **Errors and logging.** What happens when something fails, and can on-call tell?
8. **API and backward compatibility.** Does anything that already works break?
9. **Readability and naming.** Will the next person understand it?

The order is about cost. A wrong fare charges real riders real money. A clumsy name costs a second of confusion. Both are worth a comment, but never let naming debates eat the time for correctness.

### Correctness and edge cases

Read the code and run it in your head with awkward inputs. The bugs live at the edges:

- **Empty and missing**: an empty list, a `null`, an `Optional` that is empty, a rider with no rides.
- **Boundaries**: zero minutes, exactly 60 minutes, the last dock at a station, the first day of a month.
- **Every case of an enum**: if the code checks `EBIKE`, what happens to `CARGO`?
- **Money and time**: cents in a `long`, never a `double`. Times with a time zone, never the server's clock.
- **The unhappy path**: payment declined, bike already rented, rental already ended.

A strong habit: for each `if`, ask "what about the other side?". For each loop, ask "what if it runs zero times?".

### Design and responsibilities

Next, ask whether the code lives in the right place. You learned the rules in [layered architecture](/t/module-09-architecture/topic-01-layered-architecture) and [SOLID](/t/module-06-craft/topic-02-solid).

- **The right layer.** Business rules belong in the service or domain, not in a controller or a SQL string.
- **One source of truth.** If the fare is already worked out in `FareCalculator`, a second copy of that maths will drift. Reuse it.
- **The right size.** A 120-line method, or a class that does five jobs, is hard to test and change.
- **Fits the codebase.** Does it solve a problem the way the team already solves it, or invent a new way?

Design comments are worth the most when they come early. If you see a design problem, say so before nitpicking lines that may be rewritten.

### Reviewing the tests

Tests are code too, and they deserve a careful read. The key question: **would this test fail if the change were wrong?**

Try it in your head. Undo the fix, or break the new code. Does any test go red? A test that only checks `status().isOk()` passes even when every fare in the body is wrong.

Look for:

- **Assertions on behaviour**: the actual values, not just "no exception".
- **The edge cases** from the correctness list, each with its own test.
- **The bug itself**: a bug fix comes with a test that failed before the fix.
- **Stable tests**: no real clock, no network, no sleeping, no order between tests. You saw why in [CI](/t/module-07-tooling/topic-04-ci-basics).

If a test is missing, ask for that exact test: "please add one for a CARGO ride, 20 minutes, 600 cents".

### Security: injection, secrets and access

Three security checks fit every review:

- **Injection.** Is user input ever glued into SQL, a shell command or a file path? It must go in as a parameter instead. See [SQL injection](/t/module-08-backend/topic-08-jdbc).
- **Secrets.** No passwords, API keys or tokens in code, config files or logs. Card numbers and payment tokens never reach a log line.
- **Access.** Can rider-8 read rider-7's data by changing an id in the URL? That is BOLA, from [Spring Security](/t/module-08-backend/topic-11-spring-security). Every endpoint with an id in it needs a "does this belong to you?" check.

Security bugs are always blocking. One leaked token or one injectable query can hurt every rider at once.

### Concurrency and performance

**Concurrency.** Picture two requests running the same code at the same moment. A check-then-act, such as "is the bike free? then rent it", can let both through. You fixed this in [concurrent writes](/t/module-08-backend/topic-15-concurrent-writes) with a conditional update. Also watch for shared mutable fields in a Spring bean: one bean serves every request.

**Performance.** Look for work that grows with the data:

- A query inside a loop: the **N+1 problem** from [JPA](/t/module-08-backend/topic-09-jpa-hibernate). 1 query for 50 rides, then 50 more for their bikes.
- No limit on a list endpoint. Fine for a rider with 10 rides, painful for one with 3,000.
- Heavy work in a hot path, such as building a regex or a big object on every request.

Do not guess at speed. If you suspect a hot spot, ask for a measurement, as in [performance profiling](/t/module-09-architecture/topic-07-performance-profiling).

### Errors, logging and compatibility

**Errors.** What happens when the database is down or PayRiver says no? Watch for a `catch` that swallows the exception and carries on. Watch for a raw stack trace sent to the rider instead of a Problem Details answer.

**Logging.** When this fails at 3 a.m., will the log line tell on-call which rental, and why? Is it at the right level? Does it leak a token or an email address?

```callout jargon
A change is **backward compatible** when everything that worked before still works. Old app versions, other services and saved data keep working without changes.
```

**Compatibility.** Riders do not all update the app on the same day. Renaming a JSON field from `fareCents` to `fare` breaks every old app at once. Safer: add the new field, keep the old one, and remove it much later. Database migrations follow the same expand-then-contract steps you saw in [database design](/t/module-08-backend/topic-14-database-design).

### Readability, and what to leave to robots

Last, read for the next person. Are names honest: `summaries`, not `out`? Is a tricky line explained with a comment about *why*? Is there dead code?

What you should **not** spend review time on is formatting: spaces, braces, import order, line length. Robots do that better.

```callout jargon
A **formatter** rewrites code into one fixed style, such as google-java-format or Spotless. A **linter** reads code and warns about suspicious patterns, such as Checkstyle, PMD or Error Prone.
```

Run them in CI and make them fail the build. Then no human ever writes "missing space here", and nobody argues about tabs. If the team keeps debating a style point, settle it once in the style guide or the formatter config, not in every PR.

### How to write a review comment

A good comment is **specific, kind and useful**. It points at the line, says what is wrong, explains why it matters, and offers a way forward.

| Unhelpful | Helpful |
|---|---|
| "This is wrong." | "A CARGO ride is charged 15 a minute here, but the price is 25. Can we use `FareCalculator` instead?" |
| "Why would you do this??" | "What made you pick a `String` for `since`? A `LocalDate` would reject bad dates for us." |
| "You forgot the tests again." | "Could we add a test for a rider with no rides? I think it returns 500 today." |

Five habits:

- **Talk about the code, not the person.** "This method" or "we", not "you always".
- **Explain the why.** A rule with a reason teaches. A rule alone feels like an order.
- **Offer an alternative.** A small code suggestion is often clearest.
- **Ask when you are not sure.** "Is this called from two threads?" invites an answer, not a defence.
- **Praise what is good.** "Nice test names, they read like a spec." Praise tells people what to keep doing.

### Label the severity: Conventional Comments

When 23 comments arrive, the author needs to know which ones block the merge. **Conventional Comments** is a simple, widely used format that labels every comment:

```text
<label> [(decorations)]: <subject>

issue (blocking): riderId is glued into the SQL, so anyone can inject. Please use a parameter.
suggestion: a join would load the bikes in one query instead of one per ride.
question: is this endpoint behind the rider login?
nitpick (non-blocking): `out` could be `summaries`.
praise: the PR description with real curl output made this easy to review.
```

The common labels are `praise`, `nitpick`, `suggestion`, `issue`, `todo`, `question`, `thought`, `chore` and `note`. The decorations `(blocking)` and `(non-blocking)` remove all doubt.

```callout jargon
A **nit**, short for nitpick, is a tiny point that does not need to block the merge, such as a better name. A **blocking** comment must be fixed or settled before the change can merge.
```

### Approve, comment or request changes

When you finish, GitHub asks you to pick one of three:

- **Approve**: it can merge. Use it when nothing blocking is left. You may still leave nits and say "fix them if you agree, no need for another round".
- **Comment**: feedback without a verdict. Use it for questions, an early look at a draft, or when another reviewer owns the decision.
- **Request changes**: it should not merge yet. Use it for blocking problems: a bug, a security hole, a missing test for the main behaviour.

Request changes only for real problems, never for taste. And when you request changes, come back quickly after the fixes. A PR left waiting on your second look is blocked by you.

### Review speed

A PR waiting for review is work that is finished but helps nobody. While it waits, the author starts something else, forgets the details, and faces merge conflicts. This waiting time is called **review latency**, and it quietly slows the whole team.

Healthy teams answer a review request within one working day, often within a few hours. A practical rhythm:

- Check for review requests at natural breaks, such as after lunch and before you start new work.
- If you cannot review today, say so, and suggest someone else.
- A small PR deserves a quick look. A fast "LGTM" on 20 clean lines is a gift to your teammate.

Fast does not mean careless. It means not letting a PR sit.

### Receiving feedback

Now the other side: your PR with 23 comments. That number is normal, especially in a new codebase. Here is how professionals handle it:

- **Assume good intent.** The reviewer spent their time to make your code better. Short comments are usually busy, not rude.
- **It is about the code, not you.** Every engineer gets comments, the most senior ones too.
- **Reply to every comment.** "Done", a link to the fix, or a reason. Silence leaves the reviewer guessing.
- **Push back with reasons.** If you disagree, give facts: "I kept the `String` because the mobile app sends `2026-11-01T00:00` and `LocalDate` rejects it."
- **Take long debates to a call.** If a thread passes about three rounds, a 10-minute talk beats 20 more messages. Write the outcome in the PR so others see it.
- **Thank people.** "Good catch, thank you" costs nothing and builds trust.

```callout tip
Gojo says: when a comment stings, wait an hour before you answer. Reread it as if a friend wrote it. It usually reads kinder the second time.
```

If you and the reviewer still disagree, let the style guide or the code owner decide, then commit to the decision. Arguing past that point costs more than either choice.

### Reviewing a big PR

Sometimes a 2,000-line PR lands on you anyway. Two good moves:

**Ask to split it.** Kindly, early, and with a suggestion: "Could the rename go in its own PR? Then I can review the logic properly." This is a normal request, not an insult.

**If it cannot be split, review in passes.** Do not read 2,000 lines top to bottom.

1. Read the description and the ticket. What should this do?
2. Read the tests. They show the intended behaviour.
3. Read the core logic: the service or domain classes.
4. Then the edges: controllers, wiring, config, migrations.
5. Last, skim for names and readability.

Take breaks between passes. After about an hour, you stop seeing bugs. Leave design comments after pass three, before you spend time on small things.

### The SDE 2 expectation

At junior level, the bar is "takes feedback well and fixes the comments". At SDE 2, the bar rises. You are expected to:

- **Review others' code well**: find real bugs and design problems, not just typos.
- **Review promptly**: you are not the reason PRs wait.
- **Raise the team's bar**: write the checklist, add the linter rule that ends a repeated debate, mentor juniors through kind, clear comments.
- **Write PRs that are easy to review**: small, focused, with a clear description.

In promotion documents, reviews are evidence. "Caught a double-charge bug in review" or "reviews were the team's fastest and most thorough" are exactly the lines that get people promoted.

### In an interview

Code review shows up in the **behavioural** round, where you answer with real stories. Common questions:

- "Tell me about a time you disagreed with a teammate in a code review."
- "Tell me about feedback that was hard to hear. What did you do?"
- "How do you review someone else's code?"
- "Tell me about a time you caught an important bug before it shipped."

Answer with **STAR**: the Situation, your Task, the Action you took, and the Result. A strong disagreement story shows that you used data, stayed respectful, moved to a call when text stalled, and accepted the final decision. A later ticket covers behavioural interviews in depth.

Some coding interviews also hand you a short diff and ask "what would you comment?". Use the priority list: correctness first, then design, tests and security. Say the severity of each point out loud.

## Worked example

Here is the ride history PR waiting for you, and a full review of it.

### The pull request

PR #482, "Add ride history endpoint". The description is the one you saw earlier. CI is green. Here is the diff.

```diff
+@RestController
+public class RideHistoryController {
+
+    private final JdbcClient jdbc;
+    private final BikeRepository bikes;
+
+    public RideHistoryController(JdbcClient jdbc, BikeRepository bikes) {
+        this.jdbc = jdbc;
+        this.bikes = bikes;
+    }
+
+    @GetMapping("/riders/{riderId}/rentals")
+    public List<RideSummary> history(@PathVariable String riderId, @RequestParam String since) {
+        List<Rental> rows = jdbc.sql("select * from rental where rider_id = '" + riderId
+                + "' and started_at > '" + since + "' order by started_at desc")
+                .query(Rental.class).list();
+        List<RideSummary> out = new ArrayList<>();
+        for (Rental r : rows) {
+            Bike b = bikes.findById(r.bikeId()).orElseThrow();
+            long fare = 100 + r.minutes() * (b.type() == BikeType.EBIKE ? 30 : 15);
+            out.add(new RideSummary(r.id(), b.id(), r.minutes(), fare));
+        }
+        return out;
+    }
+}
```

And the test:

```diff
+    @Test
+    void historyWorks() throws Exception {
+        mvc.perform(get("/riders/rider-7/rentals?since=2026-11-01"))
+           .andExpect(status().isOk());
+    }
```

Before you read on, try it yourself. Walk the priority list and write down what you would say.

### The review

Here are eight comments, in the order of the priority list. Each one is pinned to a line.

**On the `long fare = ...` line:**

```text
issue (blocking): CARGO rides are charged 15 a minute here, but the price is 25.
A 20-minute CARGO ride shows 400 cents, and the rider actually paid 600.
Better still, the rental row already stores fare_cents, the amount we really
charged. Could we show that instead of working it out again? Then this list can
never disagree with the receipt, even after a price change.
```

**On the `jdbc.sql(...)` line:**

```text
issue (blocking): riderId and since are glued into the SQL, so a crafted URL can
inject. Please use parameters: "... where rider_id = :riderId and started_at > :since"
with .param("riderId", riderId). As a bonus, @RequestParam LocalDate since makes
Spring answer 400 for a bad date.
```

**On the `@GetMapping` line:**

```text
issue (blocking): any logged-in rider can read anyone's history by changing the
id in the URL (BOLA). Can we check that riderId matches the logged-in rider, and
answer 404 if not, like GET /rentals/{id} does?
```

**On the test:**

```text
issue (blocking): this test only checks for 200, so it passes even with the CARGO
bug. Could we add one row per bike type and assert the fareCents? A test for a
rider with no rides would be good too: I expect [] and 200.
```

**On the `bikes.findById` line:**

```text
suggestion: this runs one query per ride, the N+1 problem. With the stored fare we
only need the bike id, which is already on the rental row, so the lookup can go.
```

**On the method signature:**

```text
question: is there an upper limit? A rider with 3,000 rides gets all of them in one
answer. Fine to leave for SPK-1013 as the description says, if you agree.
```

**On the loop:**

```text
nitpick (non-blocking): `out`, `r` and `b` could be `summaries`, `rental` and `bike`.
```

**On the PR as a whole:**

```text
praise: the description with real curl output made this quick to review, thank you.
```

Your verdict is **Request changes**. The four blocking issues are real problems. Riders see a wrong number, the query is injectable, data leaks, and the test cannot catch any of it. None of the comments is about taste.

### What happened next

The author replied to every comment. They fixed the four blocking issues and the N+1, and renamed the variables. On the paging question, they linked SPK-1013 and you agreed. They thanked you for the CARGO catch: their test data had only CLASSIC and EBIKE bikes.

You re-reviewed the same afternoon, checked the new tests failed against the old code, and approved. Total time from first comment to merge: under a day.

Notice what the review did not contain: no comments on spacing, and no "I would have written it differently". Every comment either protected riders or made the code clearer, and each one said how much it mattered.

## Try it

This program runs the PR's fare line next to Spoke's real price list. Run it and see which bike type is wrong.

```java run
public class Main {
    enum BikeType { CLASSIC, EBIKE, CARGO }

    // The pull request's version, copied from the diff
    static long prFare(BikeType type, long minutes) {
        return 100 + minutes * (type == BikeType.EBIKE ? 30 : 15);
    }

    // Spoke's real price list: 100 cents to unlock, then a rate per minute
    static long fare(BikeType type, long minutes) {
        long perMinute = switch (type) {
            case CLASSIC -> 15;
            case EBIKE -> 30;
            case CARGO -> 25;
        };
        return 100 + minutes * perMinute;
    }

    public static void main(String[] args) {
        long minutes = 20;
        for (BikeType type : BikeType.values()) {
            long pr = prFare(type, minutes);
            long real = fare(type, minutes);
            String verdict = pr == real ? "ok" : "WRONG by " + (real - pr) + " cents";
            System.out.println(type + ": PR says " + pr + ", price list says " + real + " -> " + verdict);
        }
    }
}
```

It prints:

```text
CLASSIC: PR says 400, price list says 400 -> ok
EBIKE: PR says 700, price list says 700 -> ok
CARGO: PR says 400, price list says 600 -> WRONG by 200 cents
```

1. Set `minutes` to `0`. Is CARGO still wrong? What does that tell you about testing with only one value?
2. Add a fourth bike type, `TANDEM`, to the enum only. Which method keeps compiling and quietly gives a fare? Which one stops compiling? Which would you rather have?
3. Write the review comment for question 2 in Conventional Comments form.

## Recap

- Code review catches bugs early, spreads knowledge, keeps code consistent and teaches both sides. It is not a stage, a closed gate or a verdict on a person.
- Make your PR easy to review: small, one purpose, green CI, a description with what, why and how tested, and a self-review first.
- Review in priority order: correctness, design, tests, security, concurrency, performance, errors and compatibility, then naming. Leave formatting to formatters and linters.
- Write comments that are specific, kind and about the code. Explain why, offer an alternative, and label the severity with Conventional Comments.
- Approve when nothing blocking is left, request changes only for real problems, and answer within a day. When you receive feedback, assume good intent, reply to every comment, and push back with reasons.

## Common mistakes

**1. Nitpicking while a bug walks past.**

```text
nitpick: rename r to rental
nitpick: add a blank line here
nitpick: this comment has a typo
LGTM!  (the CARGO fare is wrong and the SQL is injectable)
```

Symptom: the PR merges with a polished name and a real bug. Riders are shown wrong fares. Fix: walk the priority list from the top. Correctness, design, tests and security come before naming.

**2. A comment with no why and no way forward.**

```text
This is wrong.
```

Symptom: the author does not know what is wrong, guesses, and the thread runs for days. Fix: say what, why and what instead. "CARGO is charged 15 a minute here, but the price is 25. Could we use the stored fare_cents?"

**3. Approving because the tests are green.**

```text
@Test void historyWorks() { ... andExpect(status().isOk()); }   // green
```

Symptom: the test passes even when every fare in the answer is wrong. Fix: ask "would this test fail if the code were wrong?". If not, ask for assertions on the real values, one per edge case.

**4. Mixing a refactor with a behaviour change.**

```text
PR #490: "Rename Rental to Ride, and fix the midnight fare bug"   (61 files changed)
```

Symptom: the reviewer cannot find the 3 lines that change behaviour among 900 renamed ones, and approves without seeing them. Fix: send the rename as its own PR first, then the fix as a small one.

**5. Treating feedback as a fight.**

```text
"It works on my machine. I'm not changing it."
```

Symptom: the thread grows to 20 replies, trust drops, and the PR waits a week. Fix: assume good intent, answer with reasons or a fix, and move to a short call after about three rounds. Write the outcome in the PR.
