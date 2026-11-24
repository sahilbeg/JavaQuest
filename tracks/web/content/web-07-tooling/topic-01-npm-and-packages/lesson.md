## Hook

Think of a recipe card for Meera Aunty's famous coaster pattern. It lists the yarn, the hook size and the steps. Anyone with the card can make the same coaster, in any kitchen.

Right now dhaaga.in has no card. It travels as a zip on a pen drive, with a folder of other people's code inside. Nobody wrote down which versions are in there. Sana's new laptop gets a different mix, and the site breaks.

A JavaScript project fixes this with one small file that lists everything it needs. A tool reads that file and fetches the same pieces on every laptop. The tool is **npm**.

## Concept

```callout jargon
A **package** is a folder of code someone published so others can reuse it, with a name and a version. **npm** (Node Package Manager) is the tool that downloads packages, and also the name of the big public store they come from.
```

npm comes with **Node.js**, the program that runs JavaScript outside the browser. When you install Node, you get the `node` and `npm` commands in your terminal.

The public store is called the **npm registry**. It holds millions of packages: test runners, date helpers, React itself. You name a package, and npm fetches it.

### Syntax: starting a project

```text
npm init -y
```

`npm init` asks some questions and writes a file called `package.json`. `-y` says "yes to every default", so it skips the questions. Run it once, in the project's top folder.

```callout jargon
**package.json** is the project's ID card. It is a JSON file in the top folder with the project's name, version, scripts and the packages it needs.
```

Here is a small one for the website:

```json
{
  "name": "dhaaga-web",
  "version": "1.0.0",
  "private": true,
  "type": "module",
  "scripts": {
    "dev": "vite",
    "build": "vite build",
    "test": "vitest run"
  },
  "dependencies": {
    "dayjs": "^1.11.13"
  },
  "devDependencies": {
    "vite": "^7.1.3",
    "vitest": "^5.1.1"
  }
}
```

- `"private": true` stops anyone from publishing the shop's code to the registry by mistake.
- `"type": "module"` tells Node that `.js` files use `import` and `export`.
- `scripts` are short names for commands. You will run them in a moment.

### Syntax: installing a package

```text
npm install <package>
npm install --save-dev <package>
```

`npm install dayjs` downloads the `dayjs` date library. It also writes it into `dependencies` in `package.json`, with the version it got. The real output is short:

```text
added 1 package, and audited 2 packages in 640ms

found 0 vulnerabilities
```

`--save-dev` (or `-D`) puts the package under `devDependencies` instead. The two lists mean different things:

| List | What goes there | Example |
|---|---|---|
| `dependencies` | Code the website itself runs in the customer's browser | `dayjs`, `react` |
| `devDependencies` | Tools only developers use while building and testing | `vite`, `vitest`, `eslint` |

```callout jargon
To **audit** is to check something against a list of known problems. `npm audit` checks your packages against a list of known security holes. "found 0 vulnerabilities" means none of yours are on it.
```

### Version ranges: ^ and ~

Package versions use **semantic versioning**: three numbers, `MAJOR.MINOR.PATCH`, like `1.11.13`.

```callout jargon
**Semantic versioning** (semver) is a promise about version numbers. PATCH goes up for a bug fix, MINOR for a new feature that breaks nothing, and MAJOR when old code might break.
```

In `package.json`, the version is usually a **range**, not one exact number. The symbol in front says how far npm may move.

| Written | Means | Would accept | Would refuse |
|---|---|---|---|
| `"1.11.13"` | exactly this | `1.11.13` | `1.11.14` |
| `"~1.11.13"` | newer patches only | `1.11.20` | `1.12.0` |
| `"^1.11.13"` | newer minors and patches | `1.19.0` | `2.0.0` |
| `"^0.4.2"` | under 1.0, only patches | `0.4.9` | `0.5.0` |

The caret `^` is the default that `npm install` writes. It trusts the promise: anything below the next MAJOR should still work.

```callout tip
Semver is a promise people make, not a rule a computer checks. Run your tests after every update, even a tiny patch.
```

### node_modules: where packages live

```callout jargon
**node_modules** is the folder where npm puts every package it downloads. It sits next to `package.json`.
```

Packages need packages too. `vite` needs a few dozen of its own. npm fetches all of them, so a project with three listed packages can have hundreds of folders in `node_modules`.

Two rules about this folder:

- Never edit anything inside it. The next install throws your change away.
- Never commit it to Git, and never zip it for a teammate. Anyone can rebuild it from `package.json` in seconds.

### The lock file

A range like `^7.1.3` can match many versions. Today it gives `7.1.3`. Next month it might give `7.4.0`. Two laptops installing a month apart can end up with different code.

```callout jargon
**package-lock.json** is the **lock file**. npm writes it on every install. It records the exact version of every package in `node_modules`, including the packages your packages needed.
```

`package.json` says what you accept. `package-lock.json` says what you actually got. Commit both to Git.

```animate
title: One project, two laptops
kind: scene
nodes:
  - { id: you, label: Your laptop, emoji: "💻", value: empty node_modules }
  - { id: registry, label: npm registry, emoji: "📦" }
  - { id: git, label: Git repository, emoji: "🗂️" }
  - { id: sana, label: Sana's laptop, emoji: "💻", value: new }
steps:
  - say: You run npm install. npm reads package.json and asks the registry for versions that fit each range.
    send: { from: you, to: registry, label: "vite ^7.1.3?" }
  - say: The registry sends back vite 7.1.3 and everything vite needs. npm fills node_modules.
    send: { from: registry, to: you, label: "7.1.3 + 40 more", back: true }
    set: { you: node_modules filled }
  - say: npm writes package-lock.json with every exact version. You commit package.json and the lock file, but not node_modules.
    send: { from: you, to: git, label: "package.json + lock" }
    set: { git: no node_modules }
  - say: Sana clones the project. She has the card and the exact list, but no packages yet.
    send: { from: git, to: sana, label: git clone, back: true }
  - say: She runs npm ci. npm ignores the ranges and fetches exactly what the lock file says.
    send: { from: sana, to: registry, label: "exactly 7.1.3" }
  - say: Same versions on both laptops. The site behaves the same for her as for you.
    send: { from: registry, to: sana, label: "7.1.3 + 40 more", back: true }
    set: { sana: same as yours }
```

### Syntax: npm ci

```text
npm ci
```

`npm ci` ("clean install") is for setting up a project someone else made. It deletes any old `node_modules`, then installs exactly what the lock file lists. It never changes `package.json` or the lock file.

If the two files disagree, `npm ci` stops with an error instead of guessing. That is a good thing: it means someone changed `package.json` and forgot to commit the new lock file.

| Command | Use it when |
|---|---|
| `npm install` | You add, remove or update a package |
| `npm ci` | You set up a fresh copy, or a robot builds the project |

### Syntax: running scripts

```text
npm run <script name>
```

`npm run build` looks up `"build"` in `scripts` and runs `vite build`. Scripts can use any tool inside `node_modules`, even though those tools are not installed on your laptop as commands.

Two names are special, so you can drop `run`: `npm test` and `npm start`.

```callout tip
Scripts are the project's shared commands. A teammate does not need to remember "vitest run". They read `scripts`, or just type `npm test`.
```

### Syntax: npx

```text
npx <tool> <arguments>
```

`npx` runs a tool from a package once, without adding a script. `npx prettier --check .` runs the Prettier in `node_modules`. If the package is not installed, `npx` offers to download it first.

## Worked example

Kavya's ticket asks for a project anyone can set up with one command. Here is how you turn the pen-drive zip into one.

**Step 1: make the card.** In the project's top folder:

```text
npm init -y
```

npm writes `package.json` with a name taken from the folder. You edit it: add `"private": true` and `"type": "module"`.

**Step 2: add the packages.** The opening-hours code uses `dayjs`, and the site is served with Vite.

```text
npm install dayjs
npm install --save-dev vite
```

`dayjs` goes under `dependencies`, because customers' browsers run it. Vite goes under `devDependencies`, because only you use it.

**Step 3: add scripts.**

```json
"scripts": {
  "dev": "vite",
  "build": "vite build"
}
```

**Step 4: keep node_modules out of Git.** Make a file called `.gitignore` in the top folder:

```text
node_modules/
dist/
```

Git now ignores both folders. `dist/` is where the finished site goes, as you will see in the next ticket.

**Step 5: commit.**

```text
git add package.json package-lock.json .gitignore src index.html
git commit -m "Make dhaaga-web an npm project"
```

**Step 6: Sana's laptop.** She needs three commands, and they work the same tomorrow or next year:

```text
git clone <repository url>
npm ci
npm run dev
```

## Try it

You need Node.js. Get the LTS ("long-term support") version from nodejs.org, then check it in a terminal:

```text
node --version
npm --version
```

Then, in a new empty folder:

1. Run `npm init -y` and open `package.json`.
2. Run `npm install dayjs`. Look in `dependencies`. Which range symbol did npm write?
3. Open `package-lock.json` and search for `"node_modules/dayjs"`. Find the exact version.
4. Delete the `node_modules` folder. Run `npm ci`. The folder comes back, the same as before.
5. Add `"hello": "node -e \"console.log('Namaste from npm')\""` under `scripts`, and run `npm run hello`.

## Recap

- npm downloads packages from the npm registry. `package.json` lists the project's name, scripts and packages.
- `dependencies` run in the customer's browser. `devDependencies` are tools for building and testing.
- `^1.11.13` accepts newer minors and patches, `~1.11.13` only newer patches.
- `node_modules` holds the downloaded code. Never edit it or commit it.
- The lock file pins exact versions. Commit it, and set up fresh copies with `npm ci`. Run shared commands with `npm run`.

## Common mistakes

**1. Committing node_modules.**

```text
git add .
git commit -m "Add website"
```

What you see: the commit has 9,000 files. Every pull request drags them along, and packages built for a Mac may not work on Linux.

Fix: add `node_modules/` to `.gitignore`. If it is already committed, run `git rm -r --cached node_modules` and commit. The files stay on your disk; Git just stops tracking them.

**2. Not committing the lock file.**

```text
git add package.json src
git commit -m "Add vitest"
```

What you see: tests pass on your laptop and fail on Sana's. She got a newer version that matched the same `^` range.

Fix: always commit `package-lock.json` together with `package.json`.

**3. Putting a build tool in dependencies.**

```json
"dependencies": {
  "vite": "^7.1.3"
}
```

What you see: nothing breaks today. But anyone reading the file thinks the shop's browser code needs Vite, and some deploy setups install it for nothing.

Fix: install tools with `npm install --save-dev vite`. Only code that runs in the browser belongs in `dependencies`.

**4. Running a script without run.**

```text
npm build
```

What you see: npm does not run your script. It prints `Unknown command: "build"` and suggests `npm run build`.

Fix: type `npm run build`. Only `test` and `start` work without `run`.
