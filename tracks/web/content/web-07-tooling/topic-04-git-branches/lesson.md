## Hook

Meera Aunty tries a new pattern on a spare ball of yarn first. If the new stitch looks wrong, she unravels the trial piece. The real blanket on her lap is never touched.

Kavya and Sana have no spare yarn. They both work on `main`, the one copy everyone ships. So when Kavya deployed her delivery-fee fix, Sana's half-typed Diwali banner went live with it.

Git gives each change its own trial piece, called a **branch**. You work there. Only finished work joins `main`.

## Concept

You already know commits from web-00: a commit is a saved snapshot of the project, with a message. Each commit remembers the one before it, so the commits form a chain.

```callout jargon
A **branch** is a movable label that points at one commit. When you commit on a branch, its label slides forward to the new commit. **HEAD** is Git's marker for "where you are now". It usually points at a branch.
```

`main` is just a branch: the one the whole team shares and deploys. A new branch copies no files. Git only writes a new label, so it takes a split second.

### Syntax: making a branch

```text
git switch -c <branch name>
```

`switch -c` creates a branch at the commit you are on, and moves you onto it. `-c` means create.

```text
git switch -c feat/diwali-banner
```

```text
Switched to a new branch 'feat/diwali-banner'
```

`git branch` lists your branches. The `*` marks the one HEAD points at:

```text
* feat/diwali-banner
  main
```

```callout tip
Name a branch by its kind and topic: `feat/diwali-banner` for a feature, `fix/delivery-fee` for a bug fix. Some teams add the ticket key, like `fix/WEB-0704-delivery-fee`.
```

### Syntax: moving between branches

```text
git switch <branch name>
```

`git switch main` moves you back to `main`. Git swaps the files in your folder to match that branch. Sana's half-done banner disappears from your editor, and comes back when you switch to her branch again.

```animate
title: Two labels, one chain of commits
kind: scene
nodes:
  - { id: a, label: "Commit A: catalogue", emoji: "📦", value: "main, HEAD", row: 0, col: 0 }
  - { id: b, label: "Commit B: banner draft", emoji: "📦", row: 0, col: 1 }
  - { id: c, label: "Commit C: delivery fix", emoji: "📦", row: 1, col: 1 }
links:
  - { from: a, to: b }
  - { from: a, to: c }
steps:
  - say: main points at commit A. HEAD points at main, so that is where you are.
    focus: [a]
  - say: Sana runs git switch -c feat/diwali-banner. A second label appears on the same commit. No files are copied.
    set: { a: "main, feat/diwali-banner (HEAD)" }
  - say: She commits her banner draft. Only her branch's label moves forward. main stays on A.
    show: [b]
    set: { a: main, b: "feat/diwali-banner (HEAD)" }
  - say: Kavya commits the delivery-fee fix on main. main moves to C. The banner draft is not part of main.
    show: [c]
    set: { a: no label, c: main }
  - say: Deploying main now ships the fix, and none of the unfinished banner.
    focus: [c]
```

### Syntax: merging

```text
git switch <branch that receives the work>
git merge <branch to bring in>
```

**Merging** brings one branch's commits into another. You stand on the branch that should receive the work, then name the branch to bring in.

A merge happens in one of two ways.

**Fast-forward.** If `main` got no new commits since you branched, Git just slides the `main` label forward. No new commit is made.

```text
Updating 1c0b7aa..3f2a1c4
Fast-forward
 src/delivery.js | 2 +-
 1 file changed, 1 insertion(+), 1 deletion(-)
```

**Merge commit.** If both branches got new commits, Git makes a new commit with two parents that joins them.

```text
Merge made by the 'ort' strategy.
 src/banner.js | 4 ++++
 1 file changed, 4 insertions(+)
```

```callout jargon
A **merge commit** is a commit with two parents. It joins two lines of history into one. `ort` is the name of Git's usual way of combining them; you can ignore it.
```

### Merge conflicts

Git merges line by line. If the two branches changed different lines, Git combines them by itself. If both changed the same line, Git cannot know which one is right. That is a **merge conflict**.

```text
Auto-merging src/offers.js
CONFLICT (content): Merge conflict in src/offers.js
Automatic merge failed; fix conflicts and then commit the result.
```

Git pauses the merge and writes both versions into the file, between **conflict markers**:

```text
<<<<<<< HEAD
export const BANNER = "Free delivery in Pune on orders above ₹999";
=======
export const BANNER = "Diwali: 10% off orders above ₹1,500";
>>>>>>> feat/diwali-banner
```

- From `<<<<<<< HEAD` to `=======` is your side: the branch you are on.
- From `=======` to `>>>>>>>` is the incoming side: the branch you are merging in.

`git status` shows what is still unresolved:

```text
On branch main
You have unmerged paths.
  (fix conflicts and run "git commit")
  (use "git merge --abort" to abort the merge)

Unmerged paths:
  (use "git add <file>..." to mark resolution)
	both modified:   src/offers.js
```

### Syntax: finishing a conflicted merge

```text
git add <fixed file>
git commit
```

1. Open the file. Edit the lines into the final code you want. Often that keeps parts of both.
2. Delete all three marker lines.
3. Check the site still works: `npm run dev`, `npm test`.
4. `git add src/offers.js` tells Git this file is fixed.
5. `git commit` finishes the merge. Git fills in a message like `Merge branch 'feat/diwali-banner'`.

```callout tip
In the middle of a messy merge, `git merge --abort` puts everything back the way it was before `git merge`. Nothing is lost, and you can try again.
```

### Syntax: deleting a merged branch

```text
git branch -d <branch name>
```

Once a branch is merged, its work lives in `main`. Delete the label to keep the list short:

```text
Deleted branch feat/diwali-banner (was 9b7e0d2).
```

`-d` refuses if the branch has commits that are not merged anywhere, so you cannot lose work by accident.

## Worked example

The rule from Kavya's ticket: every change gets its own branch. Here is the Diwali banner done that way, including the conflict that hit it.

**Step 1: start from main, and branch.**

```text
git switch main
git switch -c feat/diwali-banner
```

**Step 2: work and commit, as often as you like.** Sana edits `src/offers.js` and `src/style.css`:

```text
git add src/offers.js src/style.css
git commit -m "Add the Diwali offer banner"
```

The banner is unfinished for two days. It does not matter: `main` does not have it.

**Step 3: meanwhile, a fix lands on main.** Kavya makes `fix/delivery-fee`, fixes it, merges it into `main` and deploys. Her fix also reworded the same `BANNER` line.

**Step 4: merge the banner into main.**

```text
git switch main
git merge feat/diwali-banner
```

Both branches changed `BANNER`, so Git stops with a conflict in `src/offers.js`.

**Step 5: resolve.** Neither side is right on its own. Customers need both messages, so Sana keeps both, on one line, and deletes the markers:

```js
export const BANNER = "Diwali: 10% off orders above ₹1,500 · Free delivery in Pune above ₹999";
```

```text
git add src/offers.js
git commit
git branch -d feat/diwali-banner
```

`git log --oneline --graph` now shows two lines of history joining at the merge commit:

```text
*   5d81e2a Merge branch 'feat/diwali-banner'
|\
| * 9b7e0d2 Add the Diwali offer banner
* | 3f2a1c4 Fix the free delivery limit
|/
* 1c0b7aa Draw the catalogue from JSON
```

## Try it

Make a conflict on purpose in a throwaway folder:

```text
mkdir git-try
cd git-try
git init -b main
echo "Delivery: 60" > fees.txt
git add fees.txt
git commit -m "Add delivery fee"
```

`-b main` names the first branch `main`. Now change the same line on two branches:

```text
git switch -c free-above-999
echo "Delivery: 60, free above 999" > fees.txt
git commit -am "Free delivery above 999"

git switch main
echo "Delivery: 70" > fees.txt
git commit -am "Raise delivery to 70"

git merge free-above-999
```

`commit -am` stages every changed file that Git already tracks, then commits.

1. Read the `CONFLICT` line, then run `git status`.
2. Run `git merge --abort` and `git status` again. The merge is gone. Run `git merge free-above-999` again.
3. Make `fees.txt` one line: `Delivery: 70, free above 999`. Delete the markers.
4. `git add fees.txt`, then `git commit`.
5. Run `git log --oneline --graph` and find your merge commit.

## Recap

- A branch is a movable label on a commit. `git switch -c name` makes one and moves you onto it. `main` is the branch everyone ships.
- `git switch name` moves between branches, and Git swaps your files to match.
- `git merge name` brings a branch in: a fast-forward if `main` has not moved, otherwise a merge commit with two parents.
- A conflict means both sides changed the same lines. Edit, delete the markers, `git add`, then `git commit`, or back out with `git merge --abort`.
- One change, one branch. Delete it with `git branch -d` once it is merged.

## Common mistakes

**1. Working on main by accident.**

You open the project and start the banner. `git status` says:

```text
On branch main
```

What you see: your half-done work sits on `main`. The next deploy ships it, just like Friday.

Fix: look at the first line of `git status` before you start. If you have not committed yet, run `git switch -c feat/diwali-banner` now: your edits come along to the new branch.

**2. Leaving conflict markers in the file.**

```js nocompile
<<<<<<< HEAD
export const BANNER = "Free delivery in Pune on orders above ₹999";
=======
export const BANNER = "Diwali: 10% off orders above ₹1,500";
>>>>>>> feat/diwali-banner
```

What you see: Vite shows a red error overlay: the file does not parse, starting at the `<<<<<<<` line. ESLint fails on it too.

Fix: delete every `<<<<<<<`, `=======` and `>>>>>>>` line. Search the file for `<<<<<<<` before you run `git add`.

**3. Merging in the wrong direction.**

```text
git switch feat/diwali-banner
git merge main
```

What you see: `main` is unchanged. You brought `main` into your branch, not your branch into `main`.

Fix: stand on the branch that should receive the work. To finish a feature, `git switch main` first, then `git merge feat/diwali-banner`. (Merging `main` into your branch is useful too, to catch up, but it is a different job.)

**4. Switching with unsaved work.**

You edit `src/offers.js` on your branch, do not commit, and run `git switch main`.

What you see: if `main` has a different version of that file, Git refuses to switch and lists the file, so your edits are not overwritten.

Fix: commit your work on the branch first, even with a message like "WIP banner". Then switch.
