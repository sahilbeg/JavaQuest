## Hook

The stall keeps an order book. Every sale gets a line: what was sold, to whom, and when. When a customer says, "I paid for two keychains, not one," you flip back through the pages and check.

Now picture a website with no order book. Someone changes the hours, the heading breaks, and nobody knows what the page looked like before. You'd have to rebuild it from memory.

Code needs an order book too. One that remembers every version of every file, so any change can be looked at, and any mistake can be undone.

## Concept

**Version control** is a tool that records changes to your files over time. It shows who changed what, and when. It also lets you go back to an older version. **Git** is the version control tool almost every team uses.

```callout analogy
Git is the order book for your code. Each line in the book is a **commit**: a saved snapshot of your files, with a short note saying what changed.
```

A **repository**, or "repo", is a folder that Git watches. Git keeps its order book in a hidden folder called `.git` inside it. Never edit that folder by hand.

```callout jargon
**Version control** records every change to a set of files. A **repository** (repo) is a folder whose history Git records. A **commit** is one saved snapshot of the files, with a message.
```

### Three places for your work

Your changes move through three places:

1. The **working folder** holds your files as you edit them.
2. The **staging area** holds the changes you picked for the next commit. Think of it as a tray you fill before you write in the book.
3. The **commit history** holds every commit, in order.

Watch one change go all the way through, then a second one follow:

```animate
title: A change becomes a commit
kind: scene
nodes:
  - { id: work, label: Working folder, emoji: "📝", value: "index.html (new)", row: 0, col: 0 }
  - { id: stage, label: Staging area, emoji: "🧺", value: empty, row: 0, col: 1 }
  - { id: history, label: Commit history, emoji: "📚", value: no commits, row: 0, col: 2 }
  - { id: c1, label: 4b5fa63, emoji: "🔖", value: Add first page, row: 1, col: 1 }
  - { id: c2, label: 9c1d2e7, emoji: "🔖", value: Add opening days, row: 1, col: 2 }
steps:
  - say: You create index.html in the working folder. Git can see the new file, but nothing is saved in its book yet.
    focus: [work]
  - say: git add index.html puts the file on the tray, the staging area.
    send: { from: work, to: stage, label: index.html }
    set: { stage: index.html, work: index.html }
  - say: git commit -m "Add first page" saves everything on the tray as one commit. The tray is empty again.
    send: { from: stage, to: history, label: Add first page }
    set: { stage: empty, history: 1 commit }
    show: [c1]
  - say: You add the opening days to index.html. The change waits in the working folder. The first commit stays as it was.
    focus: [work]
    set: { work: "index.html (changed)" }
  - say: git add, then git commit -m "Add opening days". The second change takes the same trip.
    send: [{ from: work, to: stage, label: change }, { from: stage, to: history, label: Add opening days }]
    set: { work: index.html, history: 2 commits }
  - say: The new commit joins the history and points back to the one before it. That's how Git remembers the order.
    show: [c2]
    send: { from: c2, to: c1, label: parent }
  - say: git log --oneline reads the history newest first, 9c1d2e7 then 4b5fa63. You can go back to either one.
    focus: [c2, c1]
```

### Where you type Git commands

You type Git commands in the terminal. In VS Code, open it with **View**, **Terminal**. It starts in your project folder.

### Syntax: check that Git is installed

```bash
git --version
```

You should see a line like `git version 2.x`. On a Mac, the first time you type `git`, macOS may offer to install the **Command Line Tools**. These are Apple's free developer tools, and Git comes with them. Click Install, wait, then try again. On Windows, download Git from `git-scm.com`.

### Syntax: tell Git who you are

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
git config --global init.defaultBranch main
```

Git writes your name and email on every commit, so the team knows who made it. `--global` means the setting covers every repo on your laptop, so you do this only once. The third line names your first line of history `main`, which is what most teams use.

### Syntax: git init

```bash
git init
```

This turns the folder you're in into a repository. Run it once per project, inside the project folder.

### Syntax: git status

```bash
git status
```

This says which files changed and which are on the tray. It never changes anything, so run it as often as you like.

### Syntax: git add

```bash
git add index.html
```

This puts the changes in `index.html` on the tray. You can name several files. `git add .` adds every changed file in the folder, but check `git status` first so nothing unwanted sneaks in.

### Syntax: git commit

```bash
git commit -m "Add opening days"
```

This saves everything on the tray as one commit. `-m` lets you write the message on the same line.

A good message is short and starts with a command word, like "Fix heading typo" or "Add WhatsApp link". Keep it under about 50 characters. A message like "stuff" or "changes" tells your team nothing.

Keep each commit to one change. Small commits are easy to read and easy to undo.

### Syntax: git log

```bash
git log --oneline
```

This lists your commits, newest first. `--oneline` shows each commit as a short id and its message.

```callout jargon
A **commit id**, or **hash**, is a code like `4b5fa63`. Git gives every commit its own id, so you can point to it later.
```

### See before you save: git diff

Before you commit, check exactly what changed. Say `index.html` is committed with one line, and you add a second:

```text
% echo "<p>Open Saturday and Sunday.</p>" >> index.html
% git diff
diff --git a/index.html b/index.html
index 7d3c1a2..e81f0b4 100644
--- a/index.html
+++ b/index.html
@@ -1 +1,2 @@
 <h1>Dhaaga & Co.</h1>
+<p>Open Saturday and Sunday.</p>
```

`>>` adds a line to the end of a file. Skip the top few lines of the diff, which only name the file. A line starting with `+` was added. A line starting with `-` was removed. A line starting with a space didn't change, and is there so you can see where you are.

`git diff` shows changes that are not on the tray yet. After `git add`, use `git diff --staged` to see what the next commit will save.

```callout tip
Run `git diff --staged` before every commit. It catches test lines and files you never meant to save.
```

### Undo: git restore

You make a mess of `index.html` and want the last saved version back. If it was committed, Git can bring it back:

```text
% git restore index.html
```

The file goes back to how it was at the last commit, or the last `git add`.

```callout warning
`git restore index.html` throws away your unsaved edits to that file for good. Git never kept a copy of them. Run `git diff` first to see what you would lose.
```

### What never goes in Git: .gitignore

Some files should never be committed:

- `node_modules/`: a folder of downloaded tools that later tickets use. It can hold thousands of files, and anyone can download it again.
- `.DS_Store`: hidden files macOS drops into folders on its own.
- `.env`: secrets like passwords and keys. Anyone who can read the repo could read them, and the history keeps them even after you delete them.

A file called `.gitignore` lists names Git should pretend not to see, one per line:

```text
node_modules/
.DS_Store
.env
```

Commit the `.gitignore` itself, so everyone working on the site shares it.

## Worked example

Here's a full terminal session, starting the Dhaaga site's history. Lines starting with `%` are what you type. The rest is what Git prints.

```text
% mkdir dhaaga-site
% cd dhaaga-site
% git init
Initialized empty Git repository in /Users/you/dhaaga-site/.git/
% echo "<h1>Dhaaga & Co.</h1>" > index.html
% git status
On branch main

No commits yet

Untracked files:
  (use "git add <file>..." to include in what will be committed)
	index.html

nothing added to commit but untracked files present (use "git add" to track)
% git add index.html
% git commit -m "Add first page"
[main (root-commit) 4b5fa63] Add first page
 1 file changed, 1 insertion(+)
 create mode 100644 index.html
% git log --oneline
4b5fa63 (HEAD -> main) Add first page
```

1. `mkdir` makes the folder and `cd` moves into it.
2. `git init` creates the repository.
3. `echo` writes one line into a new file, `index.html`. A single `>` creates or replaces the file.
4. `git status` lists the file as **untracked**: Git sees it, but isn't recording it yet.
5. `git add` puts it on the tray, and `git commit` saves it. `root-commit` means this is the very first commit.
6. `git log --oneline` shows the history: one commit, with id `4b5fa63`.

`HEAD -> main` marks the commit you're on now. `main` is the name of your line of history, called a **branch**.

## Try it

Open VS Code's terminal and follow along.

1. Tell Git who you are, if you haven't yet: the three `git config --global` lines above.
2. Make a practice folder: `mkdir ~/dhaaga-practice` then `cd ~/dhaaga-practice`
3. Start a repository: `git init`
4. Create a page: `echo "<h1>Dhaaga & Co.</h1>" > index.html`
5. Check the state with `git status`, then stage and commit: `git add index.html` and `git commit -m "Add first page"`
6. Add a line: `echo "<p>Open Saturday and Sunday.</p>" >> index.html`
7. Run `git diff` and find the line marked `+`. Then `git add index.html` and `git commit -m "Add opening days"`
8. Run `git log --oneline`. You should see two commits, newest at the top.

```callout tip
Lost? Run `git status`. It tells you where your changes are, and often suggests the next command.
```

## Recap

- **Version control** records every change, so you can see the history and undo mistakes. **Git** is the tool almost everyone uses.
- `git init` makes a folder a **repository**. Set your name and email once with `git config --global`.
- `git add` puts changes in the **staging area**, and `git commit -m "..."` saves them as a **commit**.
- `git status`, `git log --oneline` and `git diff` show where things are, the history, and the exact lines that changed.
- `git restore` brings back the last saved version, and `.gitignore` keeps tools and secrets out of Git.

## Common mistakes

**1. Committing without adding first.**

```bash
git commit -m "Add opening days"
```

Git says: `nothing added to commit but untracked files present`, or `no changes added to commit`. The tray is empty. Run `git add index.html` first, then commit.

**2. Running Git outside the project folder.**

```bash
cd ~
git status
```

Git says: `fatal: not a git repository (or any of the parent directories): .git`. You're in a folder Git doesn't watch. `cd` into your project folder, or run `git init` if it's a new project.

**3. Name and email not set.**

```bash
git commit -m "Add first page"
```

Git says `Author identity unknown`, or it guesses a name from your computer and warns you. Run the two `git config --global` lines for your name and email, then commit again.

**4. Committing secrets.**

```bash
git add .
git commit -m "Add everything"
```

If `.env` was in the folder, its passwords are now in the history, even if you delete the file later. Add a `.gitignore` before your first `git add .`, and check `git status` before every commit.
