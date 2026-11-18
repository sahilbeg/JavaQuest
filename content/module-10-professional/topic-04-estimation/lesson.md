## Hook

Ask a weather forecaster "will it rain on Saturday?" on Monday. A good one does not say "yes" or "no". They say "60% chance", and they update it every day. By Friday they are much more sure. Nobody calls that a failure. That is how forecasts work.

Software estimates are forecasts too. Bramford's city council wants Spoke bikes on its streets, and Jiraiya must tell them when. A guess that slips three times would hurt Spoke's name in a new city. In this ticket you learn how engineers turn "how long will it take?" into a plan, a range and a date they can defend.

## Concept

### Why estimates are hard, and still useful

An **estimate** is a forecast of how much work something needs, made before you know everything. Three things make it hard:

- **Unknowns.** You cannot know which library has a bug, or which test will be flaky, until you meet it.
- **Forgotten work.** People estimate the coding and forget the tests, reviews, docs and rollout.
- **Optimism.** People picture the day where everything goes right. That day is rare.

```callout jargon
The **planning fallacy** is the human habit of underestimating how long our own work will take, even when we know similar work ran late before.
```

So why estimate at all? Because people must make decisions before the work is done. Jiraiya must tell the council a date. Tsunade must choose between Bramford and a payments upgrade. Both choices need a rough cost.

An estimate is useful when it is honest about what it does not know. "Six to eight weeks, and we will know more on Friday" lets people plan. "Three weeks", said to please, does not.

### The cone of uncertainty

How wrong can an early estimate be? A classic chart from software research, known as the **cone of uncertainty**, shows that the error shrinks as a project goes on.

```callout jargon
The **cone of uncertainty** says that at the idea stage, a real duration may be anywhere from a quarter of the estimate to four times it. As requirements and design are pinned down, the range narrows.
```

```animate
title: The cone of uncertainty narrows
kind: scene
nodes:
  - { id: idea, label: Idea, emoji: "💡", row: 0, col: 0 }
  - { id: reqs, label: Requirements, emoji: "📋", row: 0, col: 1 }
  - { id: design, label: Design, emoji: "📐", row: 0, col: 2 }
  - { id: build, label: Building, emoji: "🔨", row: 0, col: 3 }
  - { id: range, label: Range for an 8-week job, emoji: "📏", value: "?", row: 1, col: 1 }
steps:
  - say: Suppose the Bramford launch will really take 8 weeks. Nobody knows that yet.
    focus: [range]
  - say: At the idea stage, an honest range is huge, from a quarter of that to four times it.
    focus: [idea]
    set: { range: "2 to 32 weeks" }
  - say: Once the requirements are agreed, the range halves on each side.
    focus: [reqs]
    set: { range: "4 to 16 weeks" }
  - say: With a design doc reviewed, the big unknowns are answered.
    focus: [design]
    set: { range: "about 5 to 12 weeks" }
  - say: Halfway through building, real progress tells you most of what is left.
    focus: [build]
    set: { range: "about 7 to 9 weeks" }
  - say: The cone only narrows if you do the work that removes unknowns. Re-estimate at each step.
    focus: [range]
```

Two lessons follow. Never give a single number at the idea stage. And do the things that shrink the cone, such as requirements, a design doc and a spike, early.

### Breaking work down

You cannot estimate "launch Bramford". It is too big and too vague. You can estimate "add a fare table for Bramford". So you break the work down.

```callout jargon
A **work breakdown** splits a big goal into smaller and smaller pieces until each piece is a task one person can finish and check. A **deliverable** is something you can show working at the end.
```

Keep splitting until every task is **two days of focused work or less**. Small tasks are easier to estimate: people are fair at guessing "one day" and terrible at guessing "five weeks". They also show progress daily, and they reveal hidden work as you split.

A good task has a clear "done", such as "a rental at `castle-hill` is priced with Bramford fares, with tests, merged". "Work on fares" has no end, so nobody can tell if it is late.

Splitting is where the real thinking happens. Halfway through, you often find a task nobody mentioned, such as "billing needs to know the city".

### The forgotten work

The code is often less than half of the job. Lists of tasks that slip almost always miss these:

- **Tests**: unit, integration, and updating the tests your change breaks.
- **Code review**: waiting for a reviewer, then fixing the comments.
- **Docs**: the README, the design doc, runbooks and the changelog, from [writing documentation](/t/module-10-professional/topic-03-documentation).
- **Migrations**: database changes, run safely in steps, as in [shipping to production](/t/module-07-tooling/topic-07-shipping-to-production).
- **Rollout**: feature flags, a canary, monitoring, and turning the old path off.
- **Launch checks**: load tests, alerts, dashboards, a dry run of the runbook.
- **Fixes after launch**: the on-call bugs of the first week.

A useful habit: copy a checklist like this into every plan, then cross out what truly does not apply. You will see the Bramford plan's docs task land on the critical path later. Forgotten work is often what sets the date.

### Ranges, not single numbers

A single number such as "5 days" hides how sure you are. A range shows it: "4 to 7 days" says "probably about 5, but it could go wrong".

```callout jargon
A **confidence level** says how often you expect to land inside your range. "4 to 7 days, 80% confident" means: if I gave ten estimates like this, about eight would be right.
```

Wide ranges are not a sign of a bad engineer. They are honest. A narrow range on something nobody has done before is the real warning sign.

Ranges are lopsided. A task can rarely finish much faster than planned, but it can run far longer. Something going wrong adds days. Something going right saves hours. Good estimates show that tilt.

### Three-point estimates and PERT

A **three-point estimate** asks for three numbers per task:

- **O**, optimistic: everything goes right.
- **M**, most likely: what usually happens.
- **P**, pessimistic: things go wrong, but not a disaster.

```callout jargon
**PERT** (Program Evaluation and Review Technique) combines the three into one expected value, (O + 4M + P) / 6, which leans toward the most likely value. Its **spread**, (P − O) / 6, is a rough standard deviation: how far the real answer typically lands from the expected one.
```

Here it is for "Rental API changes" in the Bramford launch:

```java run
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // Rental API changes, in focused engineer-days
        double optimistic = 3, mostLikely = 5, pessimistic = 10;

        double expected = (optimistic + 4 * mostLikely + pessimistic) / 6;
        double spread = (pessimistic - optimistic) / 6;      // the standard deviation

        System.out.println(String.format(Locale.ROOT, "expected: %.1f days", expected));
        System.out.println(String.format(Locale.ROOT, "spread:   %.1f days", spread));
        System.out.println(String.format(Locale.ROOT, "likely range: %.1f to %.1f days",
                expected - spread, expected + spread));
    }
}
```

It prints:

```text
expected: 5.5 days
spread:   1.2 days
likely range: 4.3 to 6.7 days
```

The expected 5.5 days is more than the most likely 5, because the pessimistic side is longer. That is the lopsided tilt from the last card, built into the formula. Each task takes O(1) time to compute.

### Focus time and calendar time

"Five days of work" is not "done by Friday". An engineer's week holds much more than project work.

| A typical 40-hour week | Hours |
|---|---|
| Meetings: stand-up, planning, one-to-ones, demos | 6 |
| Reviewing teammates' code | 4 |
| Chat, email and questions | 3 |
| Interruptions, and getting back into the work | 2 |
| Share of on-call and support | 1 |
| **Focused project work** | **24** |

That leaves about 4 to 5 focused hours a day. So estimate in **focused days**, then convert: 6 focused days at 60% focus is 6 / 0.6 = 10 working days, two calendar weeks.

```callout jargon
**Context switching** is moving your attention from one task to another. Each switch costs time to rebuild what you were thinking, often 15 minutes or more. Five interruptions can eat an hour.
```

Calendar time also loses whole days: public holidays, vacations, a conference, your on-call week. In an on-call week, plan almost no project work. Look at the real calendar before you give a date.

### Relative sizing and planning poker

People are bad at absolute time and good at comparing. "Is this bigger than that?" is easy. So many teams size work **relatively**.

```callout jargon
A **story point** is a unit of relative size: effort, complexity and doubt together, not hours. Teams often use 1, 2, 3, 5, 8, 13. The gaps grow because nobody can tell a 13 from a 14. **T-shirt sizes** (XS, S, M, L, XL) are a looser version for early, rough planning.
```

Pick a small, well-known task as a reference, such as "add a station field to the API: 2 points". Then size the rest against it: "Bramford fares feels like twice that, so 5."

**Planning poker** sizes a task as a team. Each person picks a card in secret, and everyone shows at once. If the cards say 3, 3, 5 and 13, the 3s and the 13 explain their thinking. Often the 13 knows something everyone else missed, such as "billing doesn't know about cities". Then the team votes again.

```callout jargon
**Anchoring** is when the first number spoken pulls everyone's guesses toward it. Showing cards at the same moment avoids it.
```

### Velocity, and why points are not productivity

```callout jargon
**Velocity** is how many story points a team finishes in one sprint (a fixed work period, often two weeks), averaged over the last few sprints.
```

Velocity turns points into time. If the last three sprints finished 18, 22 and 20 points, the average is 20. With 60 points left in the **backlog**, the ordered list of work not yet started, expect about 3 sprints. Give a range, because velocity wobbles.

Velocity has traps:

- **Points are not comparable between teams.** One team's 5 is another team's 3. Each team's scale is its own.
- **Points are not productivity.** If managers reward "more points", teams start sizing everything bigger. The number rises, and nothing faster gets built.
- **Only finished work counts.** A task 90% done at the end of a sprint scores 0, which keeps the number honest.

```callout jargon
**Goodhart's law**: once a measure becomes a target, people change their behaviour to hit the number, so it stops measuring what it used to.
```

Use velocity to forecast, never to compare people or to judge effort.

### Dependencies and the critical path

Some tasks must wait for others. Rental API changes need the fare table and the stations first. These "must happen before" links form a graph: tasks are nodes, and an edge runs from a task to each task that waits for it.

```callout jargon
A **dependency** is a task that must finish before another can start. The **critical path** is the longest chain of dependent tasks through the plan. Its length is the shortest time the whole project can take.
```

To find each task's earliest finish, visit tasks in **topological order**, as in [topological sort](/t/module-04-dsa/topic-15-topological-sort). A task starts when its last dependency finishes. Watch it on four tasks, measured in focused days:

```animate
title: Earliest finish, task by task
kind: code
code: |
  int fares = 3;
  int stations = 2;
  int rentals = Math.max(fares, stations) + 5;
  int bikes = stations + 2;
  int checks = Math.max(rentals, bikes) + 3;
  System.out.println("ready after day " + checks);
steps:
  - line: 1
    vars: { fares: 3 }
    say: Fares need nothing first, so they start on day 0 and finish on day 3.
  - line: 2
    vars: { stations: 2 }
    say: Stations need nothing first either. Someone else does them at the same time, done on day 2.
  - line: 3
    vars: { rentals: 8 }
    say: Rental API changes need both. They start when the later one ends, day 3, and take 5 days.
  - line: 4
    vars: { bikes: 4 }
    say: Bikes only need stations, so they finish on day 4. They have spare time.
  - line: 5
    vars: { checks: 11 }
    say: Launch checks wait for rentals and bikes. Rentals end later, on day 8, so checks end on day 11.
  - line: 6
    out: "ready after day 11"
    say: The critical path is fares, rentals, checks. A slip on bikes changes nothing, but one day lost on rentals moves the date.
```

```callout jargon
**Slack** (or float) is how long a task can slip without moving the end date. Tasks on the critical path have zero slack. Bikes above have 4 days.
```

Visiting every task once and every dependency once takes O(V + E) time and O(V) space, for V tasks and E dependencies. That is cheap even for a plan with thousands of tasks.

### Parallel work and Brooks's law

Tasks off the critical path can run in parallel, on other people. That is why a team of three can finish sooner than one person. But only down to the length of the critical path: no number of people makes rentals start before fares end.

More people also cost something. Each pair of people is a path for questions and meetings. With n people there are n × (n − 1) / 2 pairs: 3 people have 3, 4 have 6, and 6 have 15.

```callout jargon
**Brooks's law**, from Fred Brooks's book *The Mythical Man-Month*: adding people to a late software project usually makes it later. Newcomers need training from the people already busy, and every new person adds paths to keep in sync.
```

So when a plan is late, adding people is rarely the fix. Cutting scope, removing a dependency, or moving an expert onto the critical path usually helps more.

### Buffers and Hofstadter's law

```callout jargon
**Hofstadter's law**, from Douglas Hofstadter: work always takes longer than you expect, even when you allow for Hofstadter's law. It is a joke with a true point: optimism is hard to remove.
```

The fix is a **buffer**: extra time for things that will go wrong, even if you do not know which. How you add it matters.

- **Bad: padding each task in secret.** Every task quietly gets 50% extra. Then a second law strikes: work tends to grow to fill the time it is given, known as **Parkinson's law**. The padding is spent, and the project is late anyway.
- **Good: one visible buffer at the end.** Estimate tasks honestly, then add a project buffer after the critical path. Everyone can see it, and how much of it is used up.

A common size is the gap between the expected date and the safe date, which PERT's spreads give you. The worked example does exactly that.

### Risks and spikes

```callout jargon
A **risk** is something that might happen and would hurt the plan. A **risk register** is a list of them, each with how likely it is, how bad it would be, how to lower it, and an owner.
```

For Bramford: billing (from [event-driven billing](/t/module-09-architecture/mini-project)) may assume one fare table. The dock hardware may arrive late. The city's permit may need changes. Times must use `Europe/London`.

Do the scariest, most uncertain task **first**, not last. If billing really cannot handle two cities, you want to find out in week one, not week six.

```callout jargon
A **spike** is a short, **timeboxed** investigation that answers one question, such as "can billing price a ride with a per-city fare table?". Timeboxed means a fixed limit, say one day. When time is up, you stop and report what you learned, even if the answer is "no, and here is why".
```

A spike's output is knowledge, not production code. It narrows the cone for the tasks behind it. Plan the spike as its own task, with its own small estimate.

### Milestones, the MVP and cutting scope

```callout jargon
A **milestone** is a checkpoint you can show working, such as "a staff rider can rent BR-2001 at `castle-hill` in staging". An **MVP** (minimum viable product) is the smallest version that does the job for real users on day one.
```

Milestones turn a long plan into short races. Each one proves real progress, not "80% done".

For Bramford, sort features into must have on launch day, and later. A common way is **MoSCoW**: **M**ust have, **S**hould have, **C**ould have, **W**on't have this time.

- **Must:** fares per city, the five Bramford stations, renting and returning, refusing CARGO bikes, alerts and runbooks.
- **Should:** a city switch in the rider app.
- **Could:** Bramford-only promo codes.
- **Won't (this time):** memberships in Bramford.

When the plan slips, you have three levers: the date, the scope, and the people. People are slow to help (Brooks), and the date may be promised. So **cut scope, never quality**. Skipping tests, reviews or launch checks to hit a date only moves the pain to the first night on call.

### Communicating an estimate

How you say an estimate matters as much as the number.

- **Give a range and a confidence**: "6 to 8 weeks, about 85% sure."
- **Say what it assumes**: "with three engineers, starting 30 November."
- **Say what would change it**: "if Friday's billing spike finds a problem, add up to two weeks."
- **Update early.** The moment the critical path slips, say so. Bad news in week two is a plan change. Bad news on launch eve is a crisis.
- **Never pad silently.** Show the buffer. Hidden padding gets spent, and it teaches people to cut your numbers.

```callout jargon
An **estimate** is a forecast with uncertainty. A **target** is a date the business wants. A **commitment** is a promise to deliver by a date. Mixing them up causes most planning fights: say which one you are giving.
```

If asked for a single date, give the date you are confident in, not the 50-50 one. And put the reasoning in writing, so people see more than the number.

### Tracking progress: burn-down and re-estimating

```callout jargon
A **burn-down chart** plots the work still left (in points or focused days) against time. A straight "ideal" line falls to zero on the target date. The real line shows if you are ahead or behind.
```

```text
work left (focused days)
27 |*.
   |  *.
18 |    *  .            . = ideal line
   |      * * *   .     * = real: three flat days, blocked on billing
 9 |             *    .
   |                *    .
 0 +-------------------------
    wk1   wk2   wk3   wk4   wk5
```

A flat stretch is the alarm: work is not finishing. Find out why the same day.

Track **work left**, not time spent. "I spent 4 days on a 5-day task" says nothing. "I think 3 days are left" is what the plan needs. Re-estimate what is left each week, because you know more now.

Beware the "90% done" trap. A task can sit at "nearly done" for weeks. Small tasks with a clear "done" make this hard to hide: each one is finished or it is not.

### Planning as a team

Most teams plan in short cycles.

```callout jargon
A **sprint** (or iteration) is a fixed period, often one or two weeks. At the start, the team picks work for it. At the end, it shows what is done and looks back on what to improve. The **backlog** is the ordered list of all work not yet started.
```

To decide what goes first, compare **impact** (how much it helps riders or the business) with **effort** (the estimate):

| | Low effort | High effort |
|---|---|---|
| **High impact** | Quick wins: do first | Big bets: plan carefully |
| **Low impact** | Fill-ins: when there is time | Money pits: usually skip |

Some teams use **Kanban** instead of sprints: work flows continuously, with a limit on how many tasks are in progress at once. Either way, the same skills apply: small tasks, honest sizes, visible progress, and re-planning when you learn something.

### The SDE 2 expectation

A junior engineer estimates tasks someone else broke down. A mid-level engineer (SDE 2) is expected to:

- **Own a project end to end**: from the design doc to the launch and the first weeks after it.
- **Break it down for others**: write tasks clear enough that a newer teammate can pick one up and know when it is done.
- **Find the critical path**, and keep it moving: unblock it first, review its PRs first.
- **Flag risk early**, with options: "billing needs a week more; we can cut promo codes or move the date."
- **Learn from misses**: compare estimates with what happened, and adjust.

Promotion reviews look for this. A line like "delivered Bramford on the date given in week two, and said early when promo codes had to move" gets engineers promoted.

### In an interview

The behavioural round often asks: **"Tell me about a project that slipped."** The interviewer is not looking for a project that never slipped. They want to see how you noticed, what you told people, and what you changed.

A strong answer covers:

- the goal and your estimate;
- the moment you saw the slip, and who you told, how early;
- the trade-off you chose: cut scope, moved the date, or got help;
- the result, and what you estimate differently now. This is the **STAR** structure, which you met in the code-review ticket; interview prep covers it in depth.

Other questions: "How do you estimate a project you have never done before?" (break it down, three points, spike the unknown). "How do you handle a deadline you think is impossible?" (say so early, with numbers, and offer scope options).

Note the difference from the quick sums in [system design](/t/module-09-architecture/topic-06-system-design). Those estimate load: requests a second and storage. This ticket estimates effort and time. Both rely on stated assumptions and rounded numbers.

## Worked example

Here is the plan for Spoke in Bramford, the one Jiraiya takes to the council. Three engineers are free to work on it.

### The task list

Each task is two days or less of work, or split further inside. The numbers are focused engineer-days. The forgotten work is in the list: docs and runbooks, launch checks, and a staff pilot with time to fix what it finds.

| Task | Needs first | What "done" means |
|---|---|---|
| Spike: city fares in billing | nothing | a one-page answer: can billing price by city? (timeboxed: 1 day) |
| Fares per city | the spike | a fare table per city, tested, behind a flag |
| Bramford stations | nothing | `castle-hill`, `canal-street`, `market-square`, `university`, `bramford-central`, 10 docks each, in staging |
| Rental API changes | fares, stations | rentals know their city, use its fares, and refuse CARGO in Bramford |
| Bramford bikes | stations | the `BR-` bikes registered and docked in staging |
| Rider app city switch | rental API | a rider can switch the app to Bramford |
| Docs and runbooks | rental API | README, ADR, changelog and runbooks updated |
| Launch checks | rental API, bikes, docs | load test, alerts and dashboards for Bramford, runbook dry run |
| Staff pilot and fixes | app, launch checks | staff ride for real; bugs they find are fixed |

### The critical path and the date

This program computes each task's PERT value, its earliest finish, the critical path and the dates. Tasks are listed so each comes after the tasks it needs; for a big plan you would sort them first with Kahn's algorithm.

```java run
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

public class Main {
    record Task(String id, String name, double o, double m, double p, List<String> after) {
        double expected() {
            return (o + 4 * m + p) / 6;      // PERT
        }

        double variance() {
            double spread = (p - o) / 6;
            return spread * spread;
        }
    }

    public static void main(String[] args) {
        // Focused engineer-days. Each task is listed after every task it needs.
        List<Task> plan = List.of(
                new Task("spike", "Spike: city fares in billing", 1, 1, 1, List.of()),
                new Task("fares", "Fares per city", 2, 3, 6, List.of("spike")),
                new Task("stations", "Bramford stations", 1, 2, 4, List.of()),
                new Task("rentals", "Rental API changes", 3, 5, 10, List.of("fares", "stations")),
                new Task("bikes", "Bramford bikes", 1, 2, 3, List.of("stations")),
                new Task("app", "Rider app city switch", 2, 4, 9, List.of("rentals")),
                new Task("docs", "Docs and runbooks", 1, 2, 3, List.of("rentals")),
                new Task("checks", "Launch checks", 2, 3, 5, List.of("rentals", "bikes", "docs")),
                new Task("pilot", "Staff pilot and fixes", 2, 3, 6, List.of("app", "checks")));

        Map<String, Task> byId = new HashMap<>();
        Map<String, Double> finish = new HashMap<>();     // earliest finish of each task
        Map<String, String> via = new HashMap<>();        // the task that held it up longest
        double effort = 0;
        Task last = null;
        System.out.println(String.format(Locale.ROOT, "%-28s %3s %3s %3s %8s %12s", "task", "O", "M", "P", "expected", "done by day"));
        for (Task t : plan) {
            byId.put(t.id(), t);
            double start = 0;
            for (String need : t.after()) {
                if (finish.get(need) > start) {
                    start = finish.get(need);
                    via.put(t.id(), need);
                }
            }
            finish.put(t.id(), start + t.expected());
            effort += t.expected();
            if (last == null || finish.get(t.id()) > finish.get(last.id())) {
                last = t;
            }
            System.out.println(String.format(Locale.ROOT, "%-28s %3.0f %3.0f %3.0f %8.1f %12.1f",
                    t.name(), t.o(), t.m(), t.p(), t.expected(), finish.get(t.id())));
        }

        List<String> path = new ArrayList<>();
        double variance = 0;
        for (String id = last.id(); id != null; id = via.get(id)) {
            path.add(0, byId.get(id).name());
            variance += byId.get(id).variance();
        }
        double days = finish.get(last.id());
        double spread = Math.sqrt(variance);
        double focus = 0.6;                               // about 4.8 focused hours in an 8-hour day

        System.out.println();
        System.out.println("critical path: " + String.join(" -> ", path));
        System.out.println(String.format(Locale.ROOT, "critical path: %.1f focused days, spread %.1f", days, spread));
        System.out.println(String.format(Locale.ROOT, "total effort:  %.1f focused days", effort));
        System.out.println(String.format(Locale.ROOT, "calendar, 50%% likely: %.1f weeks", days / focus / 5));
        System.out.println(String.format(Locale.ROOT, "calendar, safe date:  %.1f weeks", (days + 2 * spread) / focus / 5));
    }
}
```

It prints:

```text
task                           O   M   P expected  done by day
Spike: city fares in billing   1   1   1      1.0          1.0
Fares per city                 2   3   6      3.3          4.3
Bramford stations              1   2   4      2.2          2.2
Rental API changes             3   5  10      5.5          9.8
Bramford bikes                 1   2   3      2.0          4.2
Rider app city switch          2   4   9      4.5         14.3
Docs and runbooks              1   2   3      2.0         11.8
Launch checks                  2   3   5      3.2         15.0
Staff pilot and fixes          2   3   6      3.3         18.3

critical path: Spike: city fares in billing -> Fares per city -> Rental API changes -> Docs and runbooks -> Launch checks -> Staff pilot and fixes
critical path: 18.3 focused days, spread 1.6
total effort:  27.0 focused days
calendar, 50% likely: 6.1 weeks
calendar, safe date:  7.2 weeks
```

Read it like a lead engineer would:

- **Docs and runbooks are on the critical path.** Leave them out and rerun, and the program reports 17.7 days, with the rider app on the critical path. The date looks 0.7 days earlier, and two days of real work have no place in the plan. The date is simply wrong.
- **The rider app has only 0.7 days of slack** (it finishes on day 14.3, and the pilot waits until day 15.0). It is nearly critical, so watch it closely.
- **27 days of effort, but 18.3 on the critical path.** Three engineers fit the work in parallel. A fourth would not help: nobody can start rentals before fares.
- **The spread along the path is 1.6 days.** A task's **variance** is its spread squared. You add the variances along the path, not the spreads, then take the square root. In PERT's model, you beat the expected date about half the time, and the expected date plus two spreads about 98 times in 100. That only covers risks inside the tasks, not the ones in the next card.

The program visits each task and each dependency once: O(V + E) time and O(V) space.

### Risks, milestones and the message to Jiraiya

The risk register, scariest first:

| Risk | Likelihood | Impact | What we do | Owner |
|---|---|---|---|---|
| Billing assumes one fare table | medium | high, up to 2 more weeks | the 1-day spike, first | you |
| Dock hardware arrives late | low | high, nothing to rent | order now; track weekly | Jiraiya |
| Times shown in Riverton's zone | medium | medium, wrong receipts | tests with `Europe/London` | Rentals |
| Holidays in late December | certain | one week lost | counted in the dates below | Tsunade |

Milestones, each one something you can show:

1. **End of week 2:** the spike is answered; Bramford fares and stations are in staging, behind a flag.
2. **End of week 4:** a staff member rents `BR-2001` at `castle-hill` in staging and pays the Bramford fare; CARGO is refused.
3. **End of week 6:** launch checks pass; the staff pilot starts.
4. **Launch:** the flag turns on for every Bramford rider.

The MVP is the Must list from earlier. If the app switch slips, it ships in the week after launch, with riders picking the city from a simple menu.

Starting Monday 30 November, 6.1 weeks plus a holiday week is about 19 January, and 7.2 weeks plus a holiday week is about 26 January. The message, written down:

```text
To: Jiraiya     Subject: Bramford launch: our estimate

Bottom line: tell the council "from 1 February". We expect to be ready between 19 and 26 January.

- 50-50 date: 19 January. Safe date: 26 January (we would be surprised to miss it).
- Assumes three engineers from 30 November, and counts one week of holidays.
- 1 February leaves a few days of buffer for risks outside our plan.
- Biggest risk: billing may assume one fare table. A 1-day spike answers that by 1 December.
  If it is bad news, I will tell you that day, with options.
- If we need to save time, the app's city switch can ship a week after launch.

I will send an update every Friday, and the moment anything on the critical path slips.
```

It leads with the point, gives a range and a confidence, names its assumptions and its biggest risk, offers a scope option, and promises updates. That is an estimate Jiraiya can stand behind.

## Try it

Estimate a small job of your own: three tasks done one after another. Run it, then change the numbers.

```java run
import java.util.Locale;

public class Main {
    public static void main(String[] args) {
        // Your own tasks, done one after another, in focused days
        String[] names = {"Read the fare code", "Add the Bramford fare table", "Tests and review"};
        double[] optimistic = {0.5, 1, 1};
        double[] mostLikely = {1, 2, 1.5};
        double[] pessimistic = {2, 5, 3};
        double focus = 0.6;                     // share of a working day that is focused work

        double total = 0, variance = 0;
        for (int i = 0; i < names.length; i++) {
            double expected = (optimistic[i] + 4 * mostLikely[i] + pessimistic[i]) / 6;
            double spread = (pessimistic[i] - optimistic[i]) / 6;
            total += expected;
            variance += spread * spread;
            System.out.println(String.format(Locale.ROOT, "%-28s %.1f days", names[i], expected));
        }
        double spread = Math.sqrt(variance);
        System.out.println(String.format(Locale.ROOT, "focused days: %.1f, spread %.1f", total, spread));
        System.out.println(String.format(Locale.ROOT, "working days: %.0f to %.0f",
                Math.ceil(total / focus), Math.ceil((total + 2 * spread) / focus)));
    }
}
```

It prints 5.1 focused days and a range of 9 to 12 working days. It takes O(n) time for n tasks and O(1) extra space.

1. Add a fourth task you would have forgotten, such as "Update the README and changelog". How much does the range move?
2. Make the second task's pessimistic value 10. Why does the upper end move more than the lower end?
3. Set `focus` to 0.4, as in an on-call week. What does that do to the date?

## Recap

- Estimates are forecasts: hard because of unknowns, forgotten work and optimism, but needed for decisions. The cone of uncertainty narrows as you learn, so re-estimate.
- Break work into tasks of two days or less, including tests, reviews, docs, migrations, rollout and launch checks. Estimate in ranges, with three points and PERT: (O + 4M + P) / 6.
- Convert focused days to calendar time: about 4 to 5 focused hours a day, minus holidays. Story points and planning poker size work relatively; velocity forecasts, but points are not productivity.
- The critical path, the longest chain of dependencies, sets the date; adding people rarely shortens it. Spike the scariest risk first, keep one visible buffer, set milestones, and cut scope, never quality.
- Give a range with a confidence and assumptions, say what would change it, track work left on a burn-down, and update early. An SDE 2 owns this for their projects.

## Common mistakes

**1. Giving a single number at the idea stage.**

```text
Jiraiya: "How long for Bramford?"   You, in the corridor: "Three weeks."
```

Symptom: the number becomes a promise before anyone has broken the work down, and the launch "slips" three times. Fix: "Let me break it down; I will give you a range by Thursday." Then give a range with a confidence level.

**2. Estimating only the coding.**

```text
Fares per city: 2 days.   (tests, review, docs, migration, flag, rollout: not listed)
```

Symptom: the code is "done" on day 2, and the feature ships on day 6. Fix: use the forgotten-work checklist for every plan, and define "done" as merged, documented and released.

**3. Turning focused days straight into a date.**

```java
public class Plan {
    static final double FOCUSED_DAYS = 18.3;
    static final double WORKING_DAYS = FOCUSED_DAYS;   // assumes 8 focused hours every day
}
```

Symptom: an 18-day plan takes over 30 working days, because meetings, reviews, interruptions and holidays were never counted. Fix: divide by a focus factor such as 0.6, then check the real calendar.

**4. Padding every task in secret.**

```text
Fares per city: honest 3 days -> written down as 5 "to be safe"
```

Symptom: the padding gets spent anyway (Parkinson's law), the project is still late, and people learn to cut your numbers in half. Fix: estimate honestly, then add one visible buffer at the end of the critical path.

**5. Hiding a slip until the deadline.**

```text
Week 2: billing spike finds a problem.   Week 7: "We need two more weeks."
```

Symptom: Jiraiya learns about it after telling the council, and has no time to choose. Fix: report the slip the day you find it, with options: cut scope, move the date, or change who works on the critical path.
