## Hook

Spoke keeps a logbook for every bike. Mechanics write in pen, so nobody can quietly rub out a line. When a mechanic gets something wrong, they add a new line. "Correction: the brake pad on B-1042 was not replaced." Everyone can still see both lines.

But your own scrap paper is different. Before you copy notes into the logbook, you can scribble, erase and rewrite as much as you like. Nobody else has seen it.

Git works the same way. It has gentle undos for the shared logbook and strong erasers for your scrap paper. This ticket teaches you both, and the rule for when each one is safe.

## Concept

### Two kinds of undo

Every Git undo is one of two kinds.

- **Add a new commit** that cancels an old one. History only grows. This is safe on branches other people use.
- **Move a branch label** to a different commit. The commits you moved away from vanish from `git log`. This **rewrites history**, so it is only safe on commits nobody else has.

```callout jargon
To **rewrite history** means to replace commits with new ones, or to move a branch so some commits drop out of it. Anyone who already pulled the old commits now has a different history from yours.
```

You already know one undo from your first week: `git restore`. It throws away edits you have not committed yet. This ticket covers the undos for work you have already committed, plus a few rescue tools.

Two short names come up a lot:

```callout jargon
`HEAD~1` means "one commit before where you are now". `HEAD~2` means two commits back. You can use these anywhere Git wants a commit ID.
```

### Syntax: fix your last commit with --amend

```bash
git commit --amend -m "Add Depot station"
git commit --amend --no-edit
```

`--amend` replaces your last commit with a new one. Anything you have staged goes in too. `-m` sets a new message. `--no-edit` keeps the old message.

Here a typo slipped into the message, and `README.md` was forgotten:

```text
% git commit -m "Add Deopt station"
[main 53c534b] Add Deopt station
 1 file changed, 1 insertion(+)
% git add README.md
% git commit --amend -m "Add Depot station"
[main dfcbc57] Add Depot station
 Date: Tue Nov 17 09:00:00 2026 +0000
 2 files changed, 2 insertions(+), 1 deletion(-)
% git log --oneline
dfcbc57 Add Depot station
c7e675e Add README
edebf7c Add station list
```

Look at the ID. `53c534b` is gone from the log, and `dfcbc57` took its place. Amend did not edit the commit. It made a brand-new one. So amend only commits you have not pushed yet.

### Syntax: park half-done work with git stash

You are halfway through a change, and an urgent fix comes in. Your edits are not ready to commit. **Stash** them: Git saves them on a shelf and gives you a clean folder.

```bash
git stash push -u -m "night fare, half done"
git stash list
git stash pop
```

- `git stash push` saves your edits and cleans the folder. Plain `git stash` does the same.
- `-u` also takes **untracked** files, the new files Git does not watch yet. Without `-u`, they stay behind.
- `-m` gives the stash a name, so `git stash list` reads well later.
- `git stash pop` puts the newest stash back and removes it from the shelf.

```text
% git status --short
 M Fares.java
% git stash
Saved working directory and index state WIP on main: 1a9795b Add Fares
% git status --short
% git stash list
stash@{0}: WIP on main: 1a9795b Add Fares
```

The second `git status --short` prints nothing: the folder is clean. Now you can switch branches, make the urgent fix and come back. `git stash pop` brings `Fares.java` back, then prints `Dropped refs/stash@{0}`.

```callout tip
A stash lives only on your laptop, with no branch and a vague name. Use it for minutes or hours, not weeks. For anything longer, commit the work on a branch instead.
```

### Syntax: git revert, the safe undo

```bash
git revert <commit-id>
git revert --no-edit <commit-id>
```

`git revert` makes a **new commit** that does the exact opposite of an old one. The old commit stays in history. Git opens your editor with a message like `Revert "..."`. Save and close it. `--no-edit` skips the editor.

Last week someone pushed a wrong unlock fee to `main`:

```text
% git log --oneline
3d7885d Add e-bike fare
bb0dba9 Raise unlock fee to 120
fc6c552 Add fares
% git revert --no-edit bb0dba9
[main 58519a8] Revert "Raise unlock fee to 120"
 Date: Tue Nov 17 09:00:00 2026 +0000
 1 file changed, 1 insertion(+), 1 deletion(-)
% git log --oneline
58519a8 Revert "Raise unlock fee to 120"
3d7885d Add e-bike fare
bb0dba9 Raise unlock fee to 120
fc6c552 Add fares
```

The fee is back to 100, and the e-bike fare that came later is untouched. Nothing was removed, so everyone else just pulls one more commit. This is the undo for anything already pushed.

### Syntax: git reset moves the branch label

```bash
git reset --soft HEAD~2
git reset HEAD~1
git reset --hard HEAD~1
```

`git reset` moves your current branch label back to another commit. The commits after it drop out of `git log`. The three modes differ in what happens to the changes from those commits:

| Mode | Branch label | Staging area | Your files |
|---|---|---|---|
| `--soft` | moves back | changes stay staged | untouched |
| `--mixed` (the default) | moves back | changes are unstaged | untouched |
| `--hard` | moves back | emptied | changed back too: edits are gone |

`--soft` is perfect for turning messy local commits into one:

```text
% git log --oneline
e8a37cd wip night fare again
77b9887 wip night fare
f17c6c1 Add fares
% git reset --soft HEAD~2
% git status --short
M  fares.txt
% git commit -m "Add night fare of 60 cents"
[main af20095] Add night fare of 60 cents
 1 file changed, 1 insertion(+)
```

The `M` in the first column means "staged". Both commits' changes waited on the staging area, ready for one clean commit.

```callout warning
`git reset --hard` also wipes edits you never committed. Git never saved those, so nothing can bring them back. Run `git status` before every `--hard`.
```

### Revert or reset?

| | `git revert` | `git reset` |
|---|---|---|
| What it does | adds a new commit that undoes an old one | moves the branch label back |
| History | grows | rewritten |
| Safe on a pushed, shared branch? | yes | no |
| Typical use | undo a bad commit on `main` | tidy or throw away your own local commits |

If anyone else might have the commit, revert. If the commits exist only on your laptop, reset is fine.

### Syntax: git reflog finds "lost" commits

```bash
git reflog
git reset --hard HEAD@{1}
git branch rescue f89ee45
```

The **reflog** is Git's diary of every place `HEAD` has been: each commit, switch, reset and rebase. When a reset or rebase "loses" a commit, the commit is still there. It just has no label. The reflog still lists it.

```callout jargon
`HEAD@{1}` means "where HEAD was one move ago". `HEAD~1` means "the commit before this one". After a reset, these point to very different commits.
```

To get the commit back, move your branch to it with `git reset --hard HEAD@{n}`. Or put a new branch on it with `git branch rescue <id>`. That also rescues a branch you deleted, if you had it checked out at some point.

```animate
title: Lost with reset, found with reflog
kind: scene
nodes:
  - { id: old, label: "c93f712 Kiosk 2.4.0", emoji: "📦", row: 0, col: 0 }
  - { id: fix, label: "f89ee45 Round fares", emoji: "📦", value: newest, row: 0, col: 2 }
  - { id: branch, label: "release/2.4", emoji: "🏷️", value: "→ f89ee45", row: 1, col: 1 }
  - { id: reflog, label: Reflog, emoji: "📓", value: empty, row: 1, col: 3 }
links:
  - { from: old, to: fix }
steps:
  - say: The release/2.4 label points at f89ee45, the commit with the fare fix.
    focus: [branch, fix]
  - say: git reset --hard HEAD~1 moves the label back one commit, to c93f712.
    send: { from: branch, to: old, label: "reset --hard HEAD~1" }
    set: { branch: "→ c93f712" }
  - say: Now f89ee45 has no label, so git log stops showing it. But Git has not deleted it.
    focus: [fix]
    set: { fix: "no label, still here" }
  - say: The reflog wrote down every move. One move ago, HEAD@{1}, HEAD was on f89ee45.
    send: { from: fix, to: reflog, label: "HEAD@{1}" }
    set: { reflog: "HEAD@{1} = f89ee45" }
  - say: git reset --hard HEAD@{1} moves the label back to f89ee45. The fix is home again.
    send: { from: reflog, to: branch, label: "reset --hard HEAD@{1}" }
    set: { branch: "→ f89ee45", fix: newest }
  - say: Git may clean up unlabelled commits after about 30 days. So rescue quickly, or give the commit a branch.
    focus: [reflog]
```

The reflog lives only on your laptop. It cannot rescue a teammate's commits, and it never saw edits you did not commit.

### Syntax: git cherry-pick copies one commit

```bash
git switch release/2.4
git cherry-pick -x <commit-id>
```

`git cherry-pick` copies the change from one commit onto the branch you are on, as a new commit. The classic use is a **hotfix**: a small urgent fix that must reach a release branch without everything else on `main`.

```callout jargon
A **release branch**, such as `release/2.4`, holds exactly the code that one version runs. Only fixes go onto it, never new features. A **hotfix** is an urgent fix for code that is already live.
```

`-x` adds a line to the message that says where the copy came from:

```text
% git log -1
commit 76b7005cfdb91c0a28a248fe716bcf3139468def
Author: Intern <intern@spoke.example>
Date:   Wed Nov 18 05:03:43 2026 +0000

    Round Harbor fares up to 10 cents
    
    (cherry picked from commit ddb00c6940c051bf04c1865d34d4a3cd3af14d87)
```

Now anyone can trace the fix on `release/2.4` back to its commit on `main`. Cherry-pick only adds a commit, so it is safe on shared branches. It can hit a conflict, like a merge. Fix the file, `git add` it, then `git cherry-pick --continue`.

### Syntax: tidy your branch with git rebase -i

Before you open a pull request, your branch might hold commits like "fix typo" and "add missing test". Reviewers would rather see a few clear commits. An **interactive rebase** lets you combine, rename, reorder or drop them.

```bash
git rebase -i main
```

Git opens your editor with a **todo list**: one line per commit on your branch since `main`. The oldest commit is at the top, the opposite of `git log`.

```text
pick 7a4bb2d # Add night discount
pick 4e46ddb # fix typo
pick 9c387c1 # add missing test
pick e54a99e # Rename nightFare to lateFare

# Rebase ddb00c6..e54a99e onto ddb00c6 (4 commands)
```

Below that, Git lists every command in comments. The ones you need most:

- `pick`: keep the commit as it is.
- `squash`: melt it into the commit above, and let you write the combined message.
- `fixup`: melt it into the commit above, and keep only the message above.
- `reword`: keep the commit, but change its message.
- `drop`: throw the commit away.

You change the first word on each line, then save and close the editor:

```text
pick 7a4bb2d # Add night discount
fixup 4e46ddb # fix typo
fixup 9c387c1 # add missing test
reword e54a99e # Rename nightFare to lateFare
```

Git replays the commits in that order. For `reword`, it opens the editor once more for the new message.

```text
Successfully rebased and updated refs/heads/feat/night-fare.
% git log --oneline main..
5bf294e Rename nightFare to lateFare everywhere
ee238f3 Add night discount
```

`main..` means "commits on my branch that are not on `main`". Four commits became two, all with new IDs. If a commit hits a conflict, fix the file, `git add` it, then run `git rebase --continue`. If anything goes wrong mid-rebase, `git rebase --abort` puts everything back.

```callout tip
Git opens the editor set by `git config --global core.editor`. On a Mac with nothing set, that is Vim. In Vim, press `i` to type, then Esc, then `:wq` and Enter to save and quit. For VS Code, set the editor to `"code --wait"`.
```

### The golden rule: never rewrite shared history

Amend, reset and rebase all make new commits or drop old ones. Revert and cherry-pick only add.

```callout warning
Never rewrite commits that someone else may have pulled. Rewrite freely on your own unpushed work, or your own branch that nobody else uses. On `main` and release branches, only add commits.
```

If you rewrite a pushed commit, a normal `git push` is refused. Forcing it overwrites the server. Teammates who pulled the old commits now hold a history that no longer exists. If your own branch really needs it, use `git push --force-with-lease --force-if-includes`, as in the Git workflow ticket.

### Which undo do I need?

| I want to... | Use |
|---|---|
| Throw away edits I have not committed | `git restore <file>` |
| Fix the message or add a forgotten file to my last, unpushed commit | `git commit --amend` |
| Put half-done work aside to switch branches | `git stash push -u`, later `git stash pop` |
| Undo a commit that is already pushed | `git revert <id>` |
| Turn my last few local commits back into changes | `git reset --soft HEAD~n` |
| Throw away my local commits and edits for good | `git reset --hard <id>`, after `git status` |
| Get back a commit after a reset, rebase or deleted branch | `git reflog`, then `git reset --hard HEAD@{n}` or `git branch rescue <id>` |
| Copy one fix to another branch | `git cherry-pick -x <id>` |
| Combine or rename my branch's commits before a PR | `git rebase -i main` |

### In an interview

Interviewers love "what is the difference between `git revert` and `git reset`?" The strong answer is the two kinds of undo: revert adds a commit and is safe on shared branches, while reset moves a label and rewrites history.

Other classics:

- "You pushed a bad commit to `main`. How do you undo it?" Revert it, then push.
- "You ran `git reset --hard` and lost commits. Are they gone?" No: `git reflog`, then reset or branch to them.
- "How do you get one fix into a release without the other changes?" Cherry-pick it.
- "How do you squash commits before a merge?" `git rebase -i`, or the "Squash and merge" button.

In behavioural interviews, a story about recovering calmly with the reflog shows you understand Git, not just its commands.

## Worked example

Ticket SPK-0706. Harbor's kiosk runs `release/2.4`. The fare-rounding fix is on `main`, but so is the unfinished Depot screen. You are in the middle of your own night-fare work on `feat/night-fare`.

**Step 1: put your work aside.**

```text
% git status --short
 M FareCalculator.java
?? NOTES.md
% git stash push -u -m "night fare, half done"
Saved working directory and index state On feat/night-fare: night fare, half done
```

`??` marks the untracked `NOTES.md`. That is why you add `-u`.

**Step 2: find the fix on main.**

```text
% git switch main
Switched to branch 'main'
Your branch is up to date with 'origin/main'.
% git pull
Already up to date.
% git log --oneline
018fb06 Round Harbor fares up to 10 cents
512a7af Start Depot screen (not ready)
c93f712 Kiosk 2.4.0
```

You want `018fb06`, and not `512a7af`. Merging `main` into the release would bring both.

**Step 3: copy only the fix onto the release branch.**

```text
% git switch release/2.4
Switched to a new branch 'release/2.4'
branch 'release/2.4' set up to track 'origin/release/2.4'.
% git cherry-pick -x 018fb06
[release/2.4 f89ee45] Round Harbor fares up to 10 cents
 Date: Wed Nov 18 05:02:48 2026 +0000
 1 file changed, 2 insertions(+), 1 deletion(-)
% git log --oneline
f89ee45 Round Harbor fares up to 10 cents
c93f712 Kiosk 2.4.0
```

"Switched to a new branch" appears because this laptop had no local `release/2.4` yet. Git made one that tracks the server's copy.

**Step 4: the panic, and the rescue.** For a second you think you picked onto the wrong branch. You throw the commit away:

```text
% git reset --hard HEAD~1
HEAD is now at c93f712 Kiosk 2.4.0
% git log --oneline
c93f712 Kiosk 2.4.0
```

Then `git status` says `On branch release/2.4`. You were right all along. The reflog still knows the commit:

```text
% git reflog -4
c93f712 HEAD@{0}: reset: moving to HEAD~1
f89ee45 HEAD@{1}: cherry-pick: Round Harbor fares up to 10 cents
c93f712 HEAD@{2}: checkout: moving from main to release/2.4
018fb06 HEAD@{3}: checkout: moving from feat/night-fare to main
% git reset --hard HEAD@{1}
HEAD is now at f89ee45 Round Harbor fares up to 10 cents
```

`-4` shows the four newest lines. `HEAD@{1}` is the line just before the reset: the cherry-pick. Using `--hard` here is safe, because you checked there were no uncommitted edits.

**Step 5: push the release branch.** This reset is fine because you had not pushed yet. Now push. After the server address, Git prints:

```text
   c93f712..f89ee45  release/2.4 -> release/2.4
```

`c93f712..f89ee45` means the server's branch moved forward by your commit. Nobody's history was rewritten.

**Step 6: back to your own work.**

```text
% git switch feat/night-fare
Switched to branch 'feat/night-fare'
% git stash list
stash@{0}: On feat/night-fare: night fare, half done
% git stash pop
...
Dropped refs/stash@{0} (bc05e3b6f93518d3a6d4355e63d4c3255658d923)
% git status --short
 M FareCalculator.java
?? NOTES.md
```

Both files are back exactly as you left them. Harbor gets its fix, Depot stays hidden, and `main` was never rewritten.

## Try it

Practise every undo in a throwaway repository. Your commit IDs will differ from the ones shown.

```bash
mkdir -p ~/spoke-practice/undo-lab
cd ~/spoke-practice/undo-lab
git init -b main
echo "unlock=100" > fares.txt
git add fares.txt
git commit -m "Add unlock fee"
echo "classic=15" >> fares.txt
git add fares.txt
git commit -m "Add clasic fare"
```

1. **Amend.** Fix the typo with `git commit --amend -m "Add classic fare"`. Run `git log --oneline`. The top ID changed.
2. **Stash.** Run `echo "ebike=30" >> fares.txt`, then `git stash`. Run `cat fares.txt`: the e-bike line is gone. Run `git stash pop` and `cat fares.txt` again: it is back.
3. **Commit, then revert.** Run `git add fares.txt` and `git commit -m "Add e-bike fare"`. Then run `git revert --no-edit HEAD`. You should see:

```text
% git log --oneline
d01b461 Revert "Add e-bike fare"
da9c62e Add e-bike fare
dbf5846 Add classic fare
b161f53 Add unlock fee
% cat fares.txt
unlock=100
classic=15
```

4. **Lose two commits.** Run `git reset --hard HEAD~2`. `git log --oneline` now shows only two commits.
5. **Find them.** Run `git reflog`. Yours should look like this:

```text
dbf5846 HEAD@{0}: reset: moving to HEAD~2
d01b461 HEAD@{1}: revert: Revert "Add e-bike fare"
da9c62e HEAD@{2}: commit: Add e-bike fare
dbf5846 HEAD@{3}: reset: moving to HEAD
dbf5846 HEAD@{4}: commit (amend): Add classic fare
58af807 HEAD@{5}: commit: Add clasic fare
b161f53 HEAD@{6}: commit (initial): Add unlock fee
```

   The `reset: moving to HEAD` line came from `git stash`, which cleans your folder that way.

6. **Rescue.** Run `git reset --hard HEAD@{1}`, then `git log --oneline`. All four commits are back.
7. **Bonus.** Run `git switch -c tidy`. Make three small commits. Run `git rebase -i main`, change the last two `pick` words to `fixup`, then save and close. `git log --oneline` shows one commit where three were.

## Recap

- Undo comes in two kinds: add a commit that cancels (revert, cherry-pick) or move a label (amend, reset, rebase).
- `git commit --amend` fixes your last unpushed commit, and `git stash push -u` parks half-done work until `git stash pop`.
- `git revert` is the safe undo for pushed commits. `git reset --soft`, `--mixed` and `--hard` move your branch, and `--hard` also throws edits away.
- `git reflog` lists everywhere HEAD has been, so "lost" commits come back with `git reset --hard HEAD@{n}`.
- `git cherry-pick -x` copies one fix onto a release branch, and `git rebase -i` tidies your own branch before a PR. Never rewrite history others have pulled.

## Common mistakes

**1. Amending a commit you already pushed.**

```bash
git commit --amend -m "Add e-bike fare"
git push
```

What you see:

```text
 ! [rejected]        main -> main (non-fast-forward)
hint: Updates were rejected because the tip of your current branch is behind
hint: its remote counterpart. If you want to integrate the remote changes,
hint: use 'git pull' before pushing again.
```

Your laptop now has a different commit from the server, so Git refuses. Forcing it would break everyone who pulled. Fix: leave a pushed commit alone. To change it, push a new commit, or `git revert` it. If you have not forced anything yet, `git reset --hard origin/main` puts your branch back to match the server.

**2. git reset --hard with uncommitted edits.**

```text
% git status --short
 M fares.txt
% git reset --hard
HEAD is now at e4f08d5 Add e-bike fare
% git reflog -3
e4f08d5 HEAD@{0}: reset: moving to HEAD
e4f08d5 HEAD@{1}: commit (amend): Add e-bike fare
d7e8ea9 HEAD@{2}: commit: Add ebike fare
```

What you see: the new cargo fare line in `fares.txt` is gone. The reflog cannot help, because it only records commits, and that edit was never committed. Fix: run `git status` before any `--hard`. If you want to keep the edits, `git stash` them first, or commit them.

**3. Using reset to undo a commit on main.**

```bash
git switch main
git reset --hard HEAD~1
git push --force
```

What you see: the bad commit disappears from the server. But teammates who pulled it still have it. Their next `git pull` says `Already up to date`, because their `main` is now ahead of the server. Their next push quietly brings the bad commit back.

Fix: on shared branches, run `git revert <id>` and a normal `git push`.

**4. Reverting a merge commit without -m.**

```text
% git revert HEAD
error: commit b609a4227bd58143aac573c1ead3e144788540c8 is a merge but no -m option was given.
fatal: revert failed
```

A merge commit has two parents, so Git asks which side to keep. Fix: `git revert -m 1 <id>` keeps parent 1, the branch you merged into, usually `main`. It undoes everything the merged branch brought in.

**5. Reading the rebase todo list upside down.**

```text
pick 7a4bb2d # Add night discount
fixup 4e46ddb # fix typo
```

What you see: you expected `git log` order, so you put `fixup` on the wrong line. A `fixup` on the very first line fails with `error: cannot 'fixup' without a previous commit`. Fix: in the todo list the oldest commit is at the top. `fixup` and `squash` melt a commit into the line above it. Unsure? Run `git rebase --abort` and start again.
