// The service worker. The browser runs it in the background, apart from the page, and the page registers it with
// navigator.serviceWorker.register("/sw.js", { type: "module" }). This file is finished: it hands every job to
// the functions in offline.js, which the checks test. The checks never load this file.
import { CACHE, SHELL, chooseStrategy, precache, removeOldCaches, cacheFirst, networkFirst } from "./offline.js";

self.addEventListener("install", (event) => {
  event.waitUntil(precache(caches, SHELL));
});

self.addEventListener("activate", (event) => {
  event.waitUntil(removeOldCaches(caches, CACHE));
});

self.addEventListener("fetch", (event) => {
  const url = new URL(event.request.url);
  if (event.request.method !== "GET" || url.origin !== self.location.origin) {
    return;
  }
  const strategy = chooseStrategy(url.pathname) === "network-first" ? networkFirst : cacheFirst;
  event.respondWith(caches.open(CACHE).then((cache) => strategy(url.pathname, cache, (path) => fetch(path))));
});
