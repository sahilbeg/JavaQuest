# GitHub Actions in depth: workflows, caching, secrets and releases

A pull request from a fork tried to read Spoke's secrets, and nobody noticed until Kakashi did. Build the robot reviewer that would have caught it: `WorkflowAudit` reads a workflow file and lists what is unsafe. Then build `JobPlanner`, which works out which jobs can run side by side and how many jobs a matrix really makes, because a pipeline you can't picture is a pipeline you can't speed up.

Given to you, finished: the record `Finding(rule, where, message)` and `InvalidWorkflowException`.

## Reading YAML with SnakeYAML

```java
Object root = new Yaml(new SafeConstructor(new LoaderOptions())).load(yamlText);
```

A mapping becomes a `Map` (in file order), a list becomes a `List`, and text becomes a `String`. Numbers become `Integer`, and `true`/`false` become `Boolean`. **The trap:** SnakeYAML follows YAML 1.1, where an unquoted `on` (like `yes`, `no` and `off`) means the boolean `true`. So the `on:` block of a workflow is stored under the key `Boolean.TRUE`, not `"on"`. A quoted `"on":` stays the text `"on"`.

## 1. `Workflows` (static helpers)

- **TODO 1, `load(yamlText)`:** load the text as above and return the top-level `Map`. Throw `InvalidWorkflowException` when the text is not valid YAML (catch SnakeYAML's `YAMLException`), when the top is not a `Map`, or when there is no `jobs` value that is a `Map` with at least one job.
- **TODO 2, `onBlock(workflow)`:** the value under the key `"on"` if the map has that key, otherwise the value under `Boolean.TRUE`.
- **TODO 3, `triggers(workflow)`:** the event names, in file order, as a `LinkedHashSet<String>`. `on:` can be one name (`on: push`), a list (`on: [push, workflow_dispatch]`) or a mapping whose keys are the names. No `on:` at all gives an empty set.

## 2. `WorkflowAudit.audit(yamlText)`

Load with `Workflows.load`, then return a `List<Finding>`. An empty list means the workflow passes.

`where` says where the problem is: `"workflow"` for the whole file, `"jobs.<job>"` for a job, and `"jobs.<job>.steps[<i>]"` for a step, where `i` counts from **0**. Every `message` is a non-empty sentence. The rules:

| Rule id | Where | When |
|---|---|---|
| `BROAD_PERMISSIONS` | workflow or job | `permissions: write-all` |
| `NO_PERMISSIONS` | workflow | there is no top-level `permissions` key at all, **and** at least one job has no `permissions` key either. If every job sets its own, there is no finding |
| `NO_TIMEOUT` | job | the job has no `timeout-minutes`. Jobs that call a reusable workflow (a job-level `uses:`) are skipped, because they cannot set it |
| `UNPINNED_ACTION` | step or job | a `uses:` value (on a step, or on a job that calls a reusable workflow) that is not pinned, see below. The message must contain the `uses:` value |
| `PWN_REQUEST` | step | the triggers contain `pull_request_target`, the step's `uses:` starts with `actions/checkout@`, and its `with.ref` text contains `github.event.pull_request.head`, `github.head_ref` or `refs/pull/` |
| `SECRET_ECHOED` | step | one single line of the step's `run:` text contains `${{`, optional spaces, then `secrets.` (the `SECRET` pattern), **and** that same line contains `echo` or `printf`. At most one finding per step |

**Pinning (TODO 9).** These `uses:` values are never `UNPINNED_ACTION`:

- a local action, starting with `./`;
- a Docker image, starting with `docker://`;
- an action owned by GitHub: the owner, the text before the first `/`, is exactly `actions` or `github`. Spoke trusts these on a version tag.

Every other value is pinned only when the text after its **last** `@` is exactly 40 lowercase hex characters (`[0-9a-f]{40}`, the `FULL_SHA` pattern). A tag (`@v46`), a branch (`@main`), a short SHA (`@3f9c2a1`) or no `@` at all is unpinned.

**Order of findings.** First the workflow finding (`BROAD_PERMISSIONS` or `NO_PERMISSIONS`; a top-level `write-all` gives only `BROAD_PERMISSIONS`). Then each job in file order: its `BROAD_PERMISSIONS`, then `NO_TIMEOUT` or the job-level `UNPINNED_ACTION`, then each step in order. Inside one step: `UNPINNED_ACTION`, then `PWN_REQUEST`, then `SECRET_ECHOED`.

- **TODO 4:** the workflow-level rules.
- **TODO 5:** the job loop and the job-level rules.
- **TODO 6 to 8:** the step rules, in `checkStep`.
- **TODO 9:** `checkPin`.

## 3. `JobPlanner`

### `waves(yamlText)` returns `List<List<String>>`

A job's `needs:` is missing (no needs), one job name, or a list of names.

- **TODO 13, `needsOf`:** turn the `needs` value into a list of names. Anything other than missing, a `String` or a `List` is an `InvalidWorkflowException`.
- **TODO 10:** a need that is not the name of a job in this file: throw `InvalidWorkflowException` whose message names the job **and** the missing need. Check this before anything else.
- **TODO 11:** the first wave holds every job with no needs. Each next wave holds every job whose needs are all in earlier waves. Inside a wave, jobs keep **file order**. Kahn's algorithm does this in O(jobs + needs) time: count each job's unfinished needs, and when a wave finishes, lower the count of every job waiting on it.
- **TODO 12:** if some jobs can never get into a wave, their needs form a cycle (a job needing itself counts). Throw `InvalidWorkflowException` whose message contains the word `cycle` and the names of the stuck jobs. Never loop forever.

### `jobCount(yamlText, jobName)` returns `int`

- **TODO 14:** no job called `jobName`: throw `InvalidWorkflowException`. A job without `strategy.matrix` is **1** job.
- **TODO 15:** every matrix key except `include` and `exclude` is an **axis**, whose value must be a non-empty list (otherwise `InvalidWorkflowException`). The jobs are every combination of one value from each axis: 2 values × 3 values = 6 jobs. A matrix with **no axes** makes one job per `include` entry.
- **TODO 16, `exclude`:** remove every combination that agrees with an exclude entry on **all** of that entry's keys. `- os: windows-latest` removes every Windows combination.
- **TODO 17, `include`:** handled after `exclude`, one entry at a time, against the combinations left after `exclude`. An entry **merges in and adds no job** when at least one combination agrees with it on every *axis* key the entry mentions (keys that are not axes, such as `experimental: true`, never stop a match). If no combination agrees, the entry **adds one new job**. So an include can bring back a combination that `exclude` removed.

Compare values with `Objects.equals` on the loaded values: `21` (a number) and `'21'` (text) are different.

Press **Submit** in Cadence, or just save the file in your IDE. The checks run on their own.
