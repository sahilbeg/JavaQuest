## Hook

A Sunday haat stall packs up every evening and can set up anywhere: a table, a cloth, the goods. A shop with a kitchen behind it is different. It needs water, gas and power, wherever it opens.

A plain React app is like the stall: after the build, it's just files, and any file host will do. A Next.js app with server actions, route handlers and cookies is like the shop with a kitchen. Part of it must run on a server, all the time. Deploying it well means choosing where that kitchen lives, and how you swap in a new menu without closing the doors.

## Concept

### What next build makes

```callout jargon
To **deploy** is to put a new version of your app where customers can reach it. A **build** turns your source code into the files that actually run.
```

`next build` reads every route in `app/` and writes a `.next` folder. Inside are two kinds of things:

- **Static files**: JavaScript and CSS for the browser, with a hash of their content in each name, plus the HTML of pages rendered ahead of time.
- **Server code**: everything that runs per request, such as dynamic pages, route handlers, server actions and `proxy.js`.

`next start` then runs a Node.js server that serves both. It listens on port 3000 unless you set `PORT`. In [Production builds and caching](/t/web-07-tooling/topic-10-production-builds) you saw a build that was only files. A Next.js build is files **and** a program.

```animate
title: From merge to customers
kind: scene
nodes:
  - { id: dev, label: You, emoji: "🧑‍💻", row: 0, col: 0 }
  - { id: ci, label: CI, emoji: "🤖", value: idle, row: 0, col: 2 }
  - { id: image, label: Build v42, emoji: "📦", row: 0, col: 4 }
  - { id: servers, label: Servers, emoji: "🖥️", value: "v41", row: 2, col: 4 }
  - { id: phone, label: Customer, emoji: "📱", row: 2, col: 1 }
steps:
  - say: You merge a pull request into main.
    send: { from: dev, to: ci, label: "merge" }
  - say: CI installs exact versions, runs the tests and runs next build. A red test stops everything here.
    set: { ci: "npm ci, test, next build" }
  - say: The build is packed as one version, v42. It's built once and never changed afterwards.
    send: { from: ci, to: image, label: "pack" }
    set: { ci: green }
  - say: The servers switch to v42 one by one. v41 is kept, ready for a rollback.
    send: { from: image, to: servers, label: "deploy" }
    set: { servers: "v42 (v41 kept)" }
  - say: A customer opens dhaaga.in. The server renders dynamic pages and hands out the static files.
    send: [{ from: phone, to: servers, label: "GET /cart" }, { from: servers, to: phone, label: "HTML", back: true }]
  - say: If checkout breaks, the servers go back to v41 in one step. Nothing is rebuilt.
    set: { servers: "v41 again" }
    focus: [servers]
```

### Four ways to run it

| Option | What you run | Features |
|---|---|---|
| Node.js server | `next build`, then `next start` on any host with Node.js | all |
| Docker container | the same server, packed in an image | all |
| Static export | only files, on any static host | limited |
| Platforms and adapters | Vercel, Netlify, Cloudflare and others build and run it for you | depends on the platform |

```callout jargon
A **container** is a sealed box holding your app and everything it needs to run, built from an **image**. **Docker** is the most common tool for making images and running containers. The same image runs the same way on any machine.
```

For Dhaaga, the usual choice is a platform for its simplicity, or one Docker image on a cloud provider for control. Both run the real Next.js server, so every feature works.

### Static export and what it loses

```js
export default { output: "export" };
```

With `output: "export"` in `next.config.mjs`, `next build` writes plain HTML, CSS and JavaScript into an `out` folder. Any static host can serve it, like the old React store. But nothing runs on a server after the build, so you lose everything that needs a live request:

- server actions;
- `cookies()`, `headers()` and anything else read per request;
- `proxy.js`;
- route handlers that read the request (a `GET` that only returns fixed data still works, built once);
- dynamic routes without `generateStaticParams`, and the default image optimiser.

Dhaaga's cart cookie and Buy now action rule out a static export. A marketing site or a docs site is often a perfect fit.

### Syntax: output "standalone"

```js
export default { output: "standalone" };
```

Normally `next start` needs your whole `node_modules` folder, which can be hundreds of megabytes. With `output: "standalone"`, the build traces exactly which files the server uses and copies only those into `.next/standalone`, with a small `server.js` to start it.

That makes a lean Docker image. You copy in the `public` folder and `.next/static` yourself, or serve those from a CDN, and start it with `node server.js`.

### Build-time and run-time settings

```callout jargon
**Build time** is when `next build` runs, once. **Run time** is when the server is answering customers, for weeks. A value fixed at build time can't change without a new build.
```

- `NEXT_PUBLIC_` variables are written into the browser's JavaScript at **build time**. Change one, and you must build again.
- Other variables are read by server code at **run time**, as long as the page is rendered per request.

This matters for "build once, deploy many". You want **one** build to go to a preview site, then to production, unchanged. Server settings like the database address can differ per environment at run time. A `NEXT_PUBLIC_` value can't, because it's baked into the files.

### Caching in production

Next.js sets caching headers for you:

- **Hashed static files** under `/_next/static/` get `public, max-age=31536000, immutable`: keep for a year. A changed file gets a new name, so nobody gets a stale copy.
- **Dynamic pages**, like the cart, get `private, no-cache, no-store, max-age=0, must-revalidate`, so one customer's page is never stored and shown to another.

Pages built ahead of time, and data you cached with revalidation, live in the **Next.js server's cache**. By default each server keeps its own, on its own disk. With one server, that's fine. With three servers behind a load balancer, each has a different copy, and a revalidation on one doesn't reach the others. Teams with several servers set up a shared cache handler, for example backed by Redis.

### Several servers: version skew

```callout jargon
**Version skew** is when a customer's browser is running one version of your app while the server has moved on to another. Their next click asks for files or actions that no longer exist.
```

During a deploy, some servers run v41 and some v42 for a minute. A customer who loaded v41 may click Buy now and hit a v42 server. Two settings handle this:

- **`deploymentId`** in `next.config.mjs`. Next.js tags every request with it. If browser and server disagree, it does a full page reload instead of a broken client navigation.
- **`NEXT_SERVER_ACTIONS_ENCRYPTION_KEY`**. Next.js encrypts values captured by server actions. Every server must share one key, or an action encrypted by one server can't be read by another.

### Rolling back

Keep every build as a fixed version. A rollback is then just pointing the servers back at the last good one: seconds, with nothing rebuilt.

- **Roll back first, fix second.** Customers can't wait while you write the perfect fix.
- **Data doesn't roll back.** If v42 changed how orders are saved, v41 must still read them. Make such changes in small, compatible steps.
- **Preview every pull request.** Most platforms build a preview copy of the site, with its own address, for each pull request. Use test keys there, never production ones.

## Worked example

Here's a launch plan for dhaaga.in on its own Docker container, as a config and a Dockerfile.

```js
export default {
  output: "standalone",
  deploymentId: process.env.DEPLOYMENT_VERSION,
  poweredByHeader: false,
};
```

```text
# Dockerfile (two stages)
FROM node:22-alpine AS build
WORKDIR /app
COPY package.json package-lock.json ./
RUN npm ci
COPY . .
RUN npm test && npm run build

FROM node:22-alpine
WORKDIR /app
ENV NODE_ENV=production
COPY --from=build /app/.next/standalone ./
COPY --from=build /app/.next/static ./.next/static
COPY --from=build /app/public ./public
EXPOSE 3000
CMD ["node", "server.js"]
```

Line by line:

- **`output: "standalone"`** keeps the final image small: only the traced files, no full `node_modules`.
- **`deploymentId`** comes from CI, for example the git commit, so mixed versions reload cleanly.
- **`poweredByHeader: false`** stops sending the `X-Powered-By: Next.js` header, which only helps attackers.
- **The first stage** installs, tests and builds. A failing test means no image.
- **The second stage** copies only the standalone server, the static files and `public`. It doesn't run `npm ci` again.
- **Secrets** like the UPI key aren't in the image at all. The host passes them as run-time environment variables.

## Try it

This checks whether an app could be a static export, by comparing what it uses with what needs a live server. Run it.

```js run
const needsServer = ["server actions", "cookies()", "proxy.js", "route handler reading the request"];

function canExport(features) {
  const blocked = features.filter((feature) => needsServer.includes(feature));
  return blocked.length === 0 ? "static export works" : "needs a server: " + blocked.join(", ");
}

console.log(canExport(["static pages", "generateStaticParams", "metadata"]));
console.log(canExport(["static pages", "server actions", "cookies()"]));
```

It prints:

```text
static export works
needs a server: server actions, cookies()
```

1. Add `"proxy.js"` to the first app. What does it print now, and why can't a static host run it?
2. Dhaaga's store uses server actions for Buy now. Which two deployment options from the table would you pick between, and why?

## Recap

- `next build` makes a `.next` folder with static files and server code, and `next start` runs a Node.js server that supports every feature.
- You can run that server yourself, in a Docker container, or on a platform. A static export is only files and loses server actions, cookies, proxy and request-reading route handlers.
- `output: "standalone"` copies just the files the server needs, for small Docker images started with `node server.js`.
- `NEXT_PUBLIC_` values are fixed at build time; server variables are read at run time. Build once, and promote the same build through each environment.
- Hashed files are cached for a year and dynamic pages never. With several servers, share the cache, set a `deploymentId` and one Server Actions key, and keep old builds for one-step rollbacks.

## Common mistakes

**1. Exporting an app that needs a server.**

```js
export default { output: "export" };   // in an app with server actions and a cart cookie
```

Symptom: `next build` stops with errors about features a static export can't support, or Buy now has nothing to talk to. Fix: deploy the Next.js server with `next start`, Docker or a platform.

**2. Expecting a NEXT_PUBLIC_ change to apply without a build.**

```text
NEXT_PUBLIC_SUPPORT_PHONE changed in the host's settings at 10:00, server restarted, old number still on the page
```

Symptom: customers still see the old WhatsApp number. The value was written into the JavaScript files at build time. Fix: build and deploy again, or read the value in server code at run time.

**3. Each server with its own Server Actions key.**

```text
Three containers, each built separately, no NEXT_SERVER_ACTIONS_ENCRYPTION_KEY set
```

Symptom: some Buy now clicks fail with "Failed to find Server Action", depending on which server answers. Fix: build once and run that build everywhere, or set one shared `NEXT_SERVER_ACTIONS_ENCRYPTION_KEY`.

**4. Fixing forward while checkout is down.**

```text
10:05 checkout broken -> 10:25 hotfix 1 -> 10:50 hotfix 2 -> 11:20 working
```

Symptom: over an hour of lost Diwali orders, with every hotfix a fresh risk. Fix: roll back to the last good build in one step, then fix calmly.

**5. Production keys on preview deploys.**

```text
Preview for PR 87 uses the live UPI key
```

Symptom: a reviewer testing a preview makes a real payment, or a leaked preview exposes the live key. Fix: give previews their own test keys in the host's settings, and keep production secrets only in production.
