## Hook

Before a new pattern goes on the price board, Meera Aunty shows the sample to a friend. The friend turns it over, spots a loose stitch, and says "fix this row first". Only then does it go on sale.

Code needs the same second pair of eyes. But a branch on your laptop is invisible to everyone else. To be checked, it has to be somewhere the whole team can see.

That place is **GitHub**. The "please check my work" request is called a **pull request**.

## Concept

```callout jargon
**GitHub** is a website that keeps Git repositories online, so a team can share them. A **remote** is a copy of the repository on a server like GitHub. Its usual name is **origin**.
```

Your laptop and GitHub each have a full copy of the history. Git commands move commits between them. Nothing moves until you ask.

### Syntax: cloning

```text
git clone <url>
```

`git clone` copies a whole repository from GitHub onto your laptop, with every commit. It makes a folder named after the repository, sets `origin` to the URL, and checks out `main`.

```text
git clone https://github.com/<team>/dhaaga-web.git
cd dhaaga-web
npm ci
```

```text
Cloning into 'dhaaga-web'...
```

Day one on any team starts like this. If the project began on your laptop with `git init` instead, connect it once with `git remote add origin <url>`.

### Syntax: pushing and pulling

```text
git push -u origin <branch>
git pull
```

- `git push -u origin feat/gift-wrap` uploads your branch to GitHub. `-u` links your branch to the one on GitHub, so next time a plain `git push` is enough.
- `git pull` downloads new commits from GitHub and brings them into the branch you are on.
- `git fetch` only downloads. It updates your view of GitHub, like `origin/main`, but never touches your files.

```callout jargon
**origin/main** is your laptop's last snapshot of the `main` on GitHub. It updates whenever you fetch or pull.
```

GitHub does not take your account password for Git. When you push for the first time, you log in with a **personal access token**, a long password made only for tools, or with an **SSH key**. Never share either one, and never commit it.

### Pull requests

```callout jargon
A **pull request** (PR) is a page on GitHub that asks "please merge my branch into main". It shows the **diff**, every line you added or removed, and lets teammates comment on any line.
```

After your first push, GitHub offers a button to open one. You write:

- **a title** that says what changes: "Add gift wrap for ₹30 at checkout";
- **a description** that says why, and how to check it: "Raju asked for it. Add an amigurumi, tick Gift wrap, and the total goes up by ₹30."

Then the PR collects three things: the diff, review comments, and the robot's checks.

### Code review

```callout jargon
A **code review** is a teammate reading your diff before it merges. They **approve** it, leave comments, or **request changes**.
```

A good review asks about behaviour: "What happens when the cart is empty?" Style is already handled by Prettier and ESLint.

When a reviewer asks for a change, you do not open a new PR. You fix it on the same branch, commit and push. The PR updates by itself.

```callout tip
Keep pull requests small: one ticket, one branch, one PR. A reviewer reads 100 lines carefully. Faced with 3,000, they skim and click approve.
```

### Checks: a robot that runs your scripts

```callout jargon
**CI** (continuous integration) is a robot that builds and tests every push. On GitHub, it is called **GitHub Actions**. A **workflow** is a file that tells it what to run.
```

The workflow lives in `.github/workflows/ci.yml` in the repository:

```yaml
name: CI
on: [pull_request]
jobs:
  check:
    runs-on: ubuntu-latest
    steps:
      - uses: actions/checkout@v4
      - uses: actions/setup-node@v4
        with:
          node-version: 22
      - run: npm ci
      - run: npm run lint
      - run: npm test
      - run: npm run build
```

On every pull request, GitHub starts a fresh Linux machine. It gets your code, installs Node, and runs your scripts one by one. If any command fails, the check turns red, and the PR shows a red cross.

Notice it uses `npm ci`, not `npm install`. The robot must get exactly the versions in your lock file.

```animate
title: A pull request from push to merge
kind: scene
nodes:
  - { id: laptop, label: Your laptop, emoji: "💻", value: feat/gift-wrap }
  - { id: github, label: GitHub, emoji: "🐙", value: no PR yet }
  - { id: ci, label: GitHub Actions, emoji: "🤖", value: idle }
  - { id: kavya, label: Kavya, emoji: "👩‍💻" }
steps:
  - say: You push your branch. GitHub now has a copy everyone can see.
    send: { from: laptop, to: github, label: "git push -u origin feat/gift-wrap" }
    set: { github: branch pushed }
  - say: You open a pull request. GitHub starts the robot, which runs npm ci, lint, test and build.
    send: { from: github, to: ci, label: run workflow }
    set: { github: PR open, ci: running }
  - say: A test fails. The check goes red, and the PR shows it. Nobody can miss it.
    send: { from: ci, to: github, label: "✗ npm test", back: true }
    set: { ci: red }
  - say: You fix the code, commit and push again. The same PR updates, and the robot runs again.
    send: { from: laptop, to: github, label: git push }
    set: { ci: green }
  - say: Kavya reads the diff, asks one question, and approves.
    send: { from: kavya, to: github, label: Approve }
    set: { github: "approved, checks green" }
  - say: Green checks and an approval. The PR is merged into main, and the branch is deleted.
    set: { github: merged }
    focus: [github]
```

### Protecting main

A team can tell GitHub to refuse merges into `main` unless the rules are met. This is called **branch protection**. Common rules:

- every change arrives through a pull request, never a direct push;
- the checks must be green;
- at least one reviewer must approve.

With these on, last week's mistake, merging a PR with a failing test, is no longer possible.

### Syntax: the merge buttons

When the PR is ready, GitHub offers three ways to merge it:

| Button | What lands on `main` |
|---|---|
| Create a merge commit | all your commits, plus a merge commit |
| Squash and merge | all your commits squeezed into one new commit |
| Rebase and merge | your commits copied on top of `main`, no merge commit |

Many teams pick **squash and merge**: `main` gets one tidy commit per PR, even if the branch had ten "WIP" commits. Afterwards, delete the branch on GitHub, then on your laptop:

```text
git switch main
git pull
git branch -d feat/gift-wrap
```

## Worked example

Raju asked for gift wrap at ₹30. You do it the new way.

**Step 1: fresh main, new branch.**

```text
git switch main
git pull
git switch -c feat/gift-wrap
```

`git pull` first, so your branch starts from the newest `main`.

**Step 2: code, check, commit, push.**

```text
npm run lint
npm test
git add src/checkout.js src/checkout.test.js
git commit -m "Add gift wrap for ₹30 at checkout"
git push -u origin feat/gift-wrap
```

**Step 3: open the PR.** Title: "Add gift wrap for ₹30 at checkout". Description: why, and how to try it.

**Step 4: the checks.** The robot runs. `npm run build` fails, because `checkout.js` imports `./Money.js` but the file is called `money.js`. Your Mac did not mind the capital letter. The Linux machine did. (You will meet this bug again in INC-07.) You fix the import and push. Green.

**Step 5: review.** Kavya comments on one line: "Does gift wrap count towards free delivery above ₹999?" It should not. You add a test for it, fix the code, and push. She approves.

**Step 6: merge and tidy.** Squash and merge, delete the branch, then `git switch main`, `git pull`, `git branch -d feat/gift-wrap`.

## Try it

You do not need a GitHub account for this course. If you have one, try this with a new, empty repository of your own:

1. Push the Vite project from ticket 2 to it: `git remote add origin <url>`, then `git push -u origin main`.
2. Make a branch, change the heading, commit and push it.
3. Open a pull request on GitHub. Read the "Files changed" tab.
4. Add the `ci.yml` above in another commit and push. Watch the "Checks" tab.
5. Merge with "Squash and merge", then tidy up on your laptop.

## Recap

- GitHub keeps the repository online. `origin` is its usual name; `git clone` copies it to your laptop.
- `git push -u origin branch` uploads a branch, `git pull` brings new commits in, and `git fetch` only downloads.
- A pull request asks to merge a branch, with the diff, review comments and checks. Fix feedback on the same branch and push.
- GitHub Actions runs your scripts on every PR. Branch protection refuses merges without green checks and an approval.
- Keep PRs small, merge when green and approved, then delete the branch.

## Common mistakes

**1. Pushing a branch without -u the first time.**

```text
git push
```

What you see:

```text
fatal: The current branch feat/gift-wrap has no upstream branch.
```

Fix: the first push of a branch needs `git push -u origin feat/gift-wrap`. After that, `git push` alone works.

**2. Opening a new PR for every fix.**

Kavya asks for a change, so you make `feat/gift-wrap-2` and open a second PR.

What you see: two PRs, with the comments on the first, and the fix on the second. Nobody knows which to merge.

Fix: commit the fix on the same branch and push. The open PR updates by itself, with the whole conversation in one place.

**3. Merging while a check is red.**

What you see: `main` now fails its tests. The next person to branch from it inherits the failure, and nobody can tell which change broke it.

Fix: never merge on red. Read the failing step's log in the Checks tab, fix it on the branch, and push. Turn on branch protection so GitHub refuses red merges.

**4. Using npm install in the workflow.**

```yaml
- run: npm install
```

What you see: the robot may install newer versions than your lock file says, so it passes or fails for reasons nobody can repeat on a laptop.

Fix: use `npm ci` in every workflow. It installs exactly what the lock file lists.
