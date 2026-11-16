## Hook

Every Spoke station has a maintenance logbook. Each time a mechanic fixes a bike, they write an entry. The entry says what they fixed, who did it, and when.

Months later, a bike starts squeaking. The mechanic opens the logbook and reads back through the entries. They see that a new brake pad went on last Tuesday. Now they know where to look.

Code needs a logbook too. Your kiosk greeting works today. Tomorrow someone might change it and break it. A logbook for code lets the team see every change and go back to one that worked.

## Concept

**Version control** is a tool that records changes to your files over time. Teams use it to see who changed what. They also use it to undo a bad change. **Git** is the version control tool almost every team uses.

```callout analogy
Git is the station logbook for your code. Each entry is a **commit**. The staging area is the parts tray you fill before you write the entry.
```

A **repository**, or "repo" for short, is a folder that Git watches. Git keeps its logbook in a hidden folder called `.git` inside it.

### Three places for your work

Your work moves through three places:

1. The **working folder** holds your files as you edit them.
2. The **staging area** holds the changes you picked for the next entry. This is the parts tray.
3. The **commit history** holds every saved entry. Each **commit** is one snapshot of your files, with a message.

Watch one change travel through all three, then a second change join the chain:

```animate
title: A change becomes a commit
kind: scene
nodes:
  - { id: work, label: Working folder, emoji: "📝", value: "greeting.txt (new)", row: 0, col: 0 }
  - { id: stage, label: Staging area, emoji: "🧺", value: empty, row: 0, col: 1 }
  - { id: history, label: Commit history, emoji: "📚", value: no commits, row: 0, col: 2 }
  - { id: c1, label: 3f2a1b9, emoji: "🔖", value: Add kiosk greeting, row: 1, col: 1 }
  - { id: c2, label: 8c4d2e1, emoji: "🔖", value: Add Riverside line, row: 1, col: 2 }
steps:
  - say: You create greeting.txt in the working folder. Git can see it, but nothing is saved yet.
    focus: [work]
  - say: git add greeting.txt puts the change on the parts tray, the staging area.
    send: { from: work, to: stage, label: greeting.txt }
    set: { stage: greeting.txt, work: greeting.txt }
  - say: git commit -m "Add kiosk greeting" saves everything on the tray as one commit. The tray is empty again.
    send: { from: stage, to: history, label: Add kiosk greeting }
    set: { stage: empty, history: 1 commit }
    show: [c1]
  - say: You add a line about Riverside Station to the file. The change sits in the working folder, and the commit stays as it was.
    focus: [work]
    set: { work: "greeting.txt (changed)" }
  - say: git add, then git commit -m "Add Riverside line". The second change takes the same trip.
    send: [{ from: work, to: stage, label: change }, { from: stage, to: history, label: Add Riverside line }]
    set: { work: greeting.txt, history: 2 commits }
  - say: The new commit joins the chain and points back to the one before it. That is how Git remembers the order.
    show: [c2]
    send: { from: c2, to: c1, label: parent }
  - say: git log --oneline reads the chain newest first, 8c4d2e1 then 3f2a1b9. You can go back to either one.
    focus: [c2, c1]
```

### Syntax: check that Git is installed

```bash
git --version
```

The first time you type `git`, macOS may offer to install the **Command Line Tools**. These are Apple's free developer tools, and Git comes with them. Click Install, wait for it to finish, then run `git --version` again. You should see a line like `git version 2.x`.

### Syntax: set your name once

```bash
git config --global user.name "Your Name"
git config --global user.email "you@example.com"
```

Git writes your name and email on every commit. `--global` means the setting applies to every repo on your laptop. You only do this once.

### Syntax: git init

```bash
git init
```

This turns the current folder into a repository. Run it once per project.

### Syntax: git status

```bash
git status
```

This tells you which files changed and which ones sit in the staging area. Run it often. It never changes anything.

### Syntax: git add

```bash
git add notes.txt
```

This puts the changes in `notes.txt` on the parts tray. You can name several files. `git add .` adds every changed file in the folder.

### Syntax: git commit

```bash
git commit -m "Add kiosk greeting"
```

This saves everything in the staging area as one commit. The `-m` option lets you type the message on the same line.

A good message is short and says what changed. "Fix typo in kiosk greeting" is good. "stuff" tells your team nothing.

### Syntax: git log

```bash
git log --oneline
```

This lists your commits, newest first. `--oneline` shows each commit as a short ID and its message.

```callout jargon
A commit ID, or hash, is a code like `3f2a1b9`. Git gives every commit its own ID, so you can point to it later.
```

## Worked example

Here is a full Terminal session. Lines starting with `%` are what you type. The rest is what Git prints.

```text
% mkdir kiosk
% cd kiosk
% git init
Initialized empty Git repository in /Users/you/kiosk/.git/
% echo "Hello, World!" > greeting.txt
% git status
On branch main

No commits yet

Untracked files:
  (use "git add <file>..." to include in what will be committed)
	greeting.txt

nothing added to commit but untracked files present (use "git add" to track)
% git add greeting.txt
% git status
On branch main

No commits yet

Changes to be committed:
  (use "git rm --cached <file>..." to unstage)
	new file:   greeting.txt

% git commit -m "Add kiosk greeting"
[main (root-commit) 3f2a1b9] Add kiosk greeting
 1 file changed, 1 insertion(+)
 create mode 100644 greeting.txt
% git log --oneline
3f2a1b9 (HEAD -> main) Add kiosk greeting
```

1. `mkdir` makes a folder and `cd` moves into it.
2. `git init` creates the repository.
3. `echo` writes one line into a new file, `greeting.txt`.
4. The first `git status` lists the file as **untracked**. That means Git sees it but does not watch it yet.
5. `git add` puts the file on the parts tray. The second `git status` shows it under "Changes to be committed".
6. `git commit` saves the entry. `root-commit` means this is the very first commit.
7. `git log --oneline` shows the history: one commit, ID `3f2a1b9`.

`HEAD -> main` marks the commit you are on now. `main` is the name of your line of history. Older versions of Git call it `master` instead. Both work the same way.

## Try it

Open Terminal and follow these steps.

1. Set your name and email, if you have not yet:
   `git config --global user.name "Your Name"` and `git config --global user.email "you@example.com"`
2. Make a practice folder and move into it:
   `mkdir -p ~/spoke-practice/kiosk-notes` then `cd ~/spoke-practice/kiosk-notes`
3. Start a repository: `git init`
4. Create a file: `echo "Kiosk opens at 6am" > notes.txt`
5. Check the state: `git status`
6. Stage the file: `git add notes.txt`
7. Commit it: `git commit -m "Add kiosk notes"`
8. See the history: `git log --oneline`
9. Add a line to the file: `echo "Kiosk closes at 11pm" >> notes.txt`
10. Stage and commit again: `git add notes.txt` then `git commit -m "Add closing time"`
11. Run `git log --oneline` once more. You should see two commits, newest at the top.

```callout tip
Lost? Run `git status`. It tells you where your changes are and often suggests the next command.
```

## Recap

- Version control records every change, so a team can see history and roll back.
- `git init` turns a folder into a repository.
- `git add` moves changes to the staging area, and `git commit -m` saves them as a commit.
- `git log --oneline` lists your commits, newest first.
- Set your name and email once with `git config --global`.

## Common mistakes

**Committing without adding first.**

```bash
git commit -m "Add notes"
```

Git says: `nothing added to commit but untracked files present`. The parts tray is empty. Run `git add notes.txt` first, then commit.

**Name and email not set.**

```bash
git commit -m "Add notes"
```

Git says: `Author identity unknown`. Git does not know who to write in the logbook. Sometimes Git guesses a name from your computer and warns you instead. Either way, run the two `git config --global` commands from the Concept section, then commit again.

**Running Git outside a repository.**

```bash
cd ~
git status
```

Git says: `fatal: not a git repository`. You are in a folder that Git does not watch. Use `cd` to move into your project folder, or run `git init` if it is a new project.
