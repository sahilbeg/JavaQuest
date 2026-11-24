import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { loadCatalogue } from "../src/catalogue.js";

const LOADING = "Loading the catalogue...";
const EMPTY = "No items yet. Check back soon!";
const FAILED = "Couldn't load the catalogue. Check your connection and try again.";

const catalogue = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-2001", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-3001", name: "Indigo tote bag", paise: 89900 },
];

function reply(status, body) {
  return { ok: status >= 200 && status < 300, status, json: async () => body };
}

/** fetch answers at once with this status and body. */
function serverAnswers(status, body) {
  const fetchMock = vi.fn(async () => reply(status, body));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** fetch fails with no answer at all, like flight mode. */
function networkDown() {
  vi.stubGlobal("fetch", vi.fn(async () => {
    throw new TypeError("Failed to fetch");
  }));
}

/** fetch waits until the test calls answer(status, body). */
function slowServer() {
  let finish = () => {};
  vi.stubGlobal("fetch", vi.fn(() => new Promise((resolve) => {
    finish = resolve;
  })));
  return { answer: (status, body) => finish(reply(status, body)) };
}

/** Waits for loadCatalogue, ignoring a thrown error so the checks below can explain what went wrong. */
async function settle(promise) {
  try {
    await promise;
  } catch {
    // the "never throws" check reports this
  }
}

let parts;

beforeEach(() => {
  document.body.innerHTML = `
    <p id="status"></p>
    <ul id="list"></ul>
    <button id="retry" hidden>Try again</button>
  `;
  parts = {
    status: document.querySelector("#status"),
    list: document.querySelector("#list"),
    retry: document.querySelector("#retry"),
  };
  vi.spyOn(console, "error").mockImplementation(() => {});
});

afterEach(() => {
  vi.unstubAllGlobals();
  vi.restoreAllMocks();
});

const names = () => [...document.querySelectorAll("#list li")].map((li) => li.textContent);

describe("loadCatalogue", () => {
  it("shows the loading message while it waits for the server", async () => {
    const server = slowServer();
    const done = loadCatalogue(parts);
    expect(parts.status.textContent, "set the loading text before the first await").toBe(LOADING);
    expect(parts.status.dataset.state, "data-state should be \"loading\" while waiting").toBe("loading");
    server.answer(200, catalogue);
    await settle(done);
  });

  it("draws one list item per product and moves to ready", async () => {
    serverAnswers(200, catalogue);
    await settle(loadCatalogue(parts));
    expect(names(), "each product's name should be in its own <li>").toEqual(["Golu the elephant", "Marigold coaster set", "Indigo tote bag"]);
    expect(parts.status.dataset.state, "after the items arrive, data-state should be \"ready\"").toBe("ready");
    expect(parts.status.textContent, "the status line should be empty when the items are showing").toBe("");
  });

  it("shows the empty message when there are no items", async () => {
    serverAnswers(200, []);
    await settle(loadCatalogue(parts));
    expect(parts.status.textContent, "an empty list should show the empty message").toBe(EMPTY);
    expect(parts.status.dataset.state, "an empty list should set data-state to \"empty\", not \"error\"").toBe("empty");
  });

  it("shows the error message and the retry button when the server fails", async () => {
    serverAnswers(500, { error: "Server error" });
    await settle(loadCatalogue(parts));
    expect(parts.status.textContent, "a 500 answer should show the friendly error message").toBe(FAILED);
    expect(parts.status.dataset.state, "a 500 answer should set data-state to \"error\"").toBe("error");
    expect(parts.retry.hidden, "the Try again button should appear after a failure").toBe(false);
  });

  it("handles a dropped connection without throwing", async () => {
    networkDown();
    let error = null;
    try {
      await loadCatalogue(parts);
    } catch (e) {
      error = e;
    }
    expect(error, "loadCatalogue should catch the network error itself, not throw it").toBeNull();
    expect(parts.status.textContent, "a dropped connection should show the friendly error message").toBe(FAILED);
    expect(parts.status.dataset.state, "a dropped connection should set data-state to \"error\"").toBe("error");
    expect(parts.retry.hidden, "the Try again button should appear after a failure").toBe(false);
  });

  it("hides the retry button again when a retry works", async () => {
    networkDown();
    await settle(loadCatalogue(parts));
    serverAnswers(200, catalogue);
    await settle(loadCatalogue(parts));
    expect(parts.retry.hidden, "after a successful retry, the Try again button should be hidden").toBe(true);
    expect(parts.status.dataset.state, "after a successful retry, data-state should be \"ready\"").toBe("ready");
    expect(names().length, "the retry should draw the three products").toBe(3);
  });

  it("doesn't double the list when it loads twice", async () => {
    serverAnswers(200, catalogue);
    await settle(loadCatalogue(parts));
    await settle(loadCatalogue(parts));
    expect(names().length, "loading twice should still show 3 items; empty the list before drawing").toBe(3);
  });
});
