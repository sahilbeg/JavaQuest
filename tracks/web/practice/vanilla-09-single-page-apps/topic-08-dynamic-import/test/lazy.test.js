import { describe, it, expect, beforeEach, vi } from "vitest";
import { lazy, showLazyView } from "../src/lazy.js";

const LOADING = "Loading this page...";
const FAILED = "Couldn't load this page. Check your connection and try again.";

function heading(text) {
  const h1 = document.createElement("h1");
  h1.textContent = text;
  return h1;
}

const fakeModule = { default: ({ id }) => heading(`Order ${id}`) };

/** A promise the test finishes by hand, like a slow download. */
function deferred() {
  let resolve;
  let reject;
  const promise = new Promise((res, rej) => {
    resolve = res;
    reject = rej;
  });
  return { promise, resolve, reject };
}

describe("lazy", () => {
  it("doesn't download anything until the page is needed", () => {
    const loader = vi.fn(async () => fakeModule);
    lazy(loader);
    expect(loader.mock.calls.length, "making the lazy loader must not call loader yet").toBe(0);
  });

  it("gives the loaded module", async () => {
    const load = lazy(async () => fakeModule);
    expect(await load(), "load() should give the module that loader gave").toBe(fakeModule);
  });

  it("downloads once, even when asked twice at the same time", async () => {
    const loader = vi.fn(async () => fakeModule);
    const load = lazy(loader);
    const first = load();
    const second = load();
    expect(loader.mock.calls.length, "two quick taps on Checkout should start only one download").toBe(1);
    expect(second, "both calls should get the very same promise").toBe(first);
    await first;
    await load();
    expect(loader.mock.calls.length, "once loaded, later visits reuse the module").toBe(1);
  });

  it("tries again after a failed download", async () => {
    const loader = vi.fn().mockRejectedValueOnce(new Error("offline")).mockResolvedValueOnce(fakeModule);
    const load = lazy(loader);
    let error = null;
    try {
      await load();
    } catch (e) {
      error = e;
    }
    expect(error?.message, "a failed download should be passed on to the caller").toBe("offline");
    expect(await load(), "after a failure, the next load() should try again and give the module").toBe(fakeModule);
    expect(loader.mock.calls.length, "the loader should have been called twice: once failing, once working").toBe(2);
  });
});

describe("showLazyView", () => {
  let outlet;

  beforeEach(() => {
    document.body.innerHTML = `<main id="app"><h1>Catalogue</h1></main>`;
    outlet = document.querySelector("#app");
  });

  it("shows the loading message while the code downloads", async () => {
    const download = deferred();
    const done = showLazyView(outlet, () => download.promise, { id: "order-17" });
    expect(outlet.textContent, "the outlet should say LOADING before the module arrives").toBe(LOADING);
    download.resolve(fakeModule);
    await done;
  });

  it("draws the module's default view with the params", async () => {
    await showLazyView(outlet, async () => fakeModule, { id: "order-17" });
    expect(outlet.textContent, "the outlet should hold module.default({ id: \"order-17\" })").toBe("Order order-17");
    expect(outlet.children.length, "the loading text should be replaced by exactly one screen").toBe(1);
  });

  it("shows the friendly error when the download fails, without throwing", async () => {
    let error = null;
    try {
      await showLazyView(outlet, async () => {
        throw new TypeError("Failed to fetch dynamically imported module");
      }, {});
    } catch (e) {
      error = e;
    }
    expect(error, "showLazyView should catch the failure itself").toBeNull();
    expect(outlet.textContent, "a failed download should show the FAILED message").toBe(FAILED);
  });
});
