import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { createSearch } from "../src/search.js";

const catalogue = [
  { id: "DC-1042", name: "Golu the elephant" },
  { id: "DC-2001", name: "Marigold coaster set" },
  { id: "DC-3001", name: "Indigo tote bag" },
  { id: "DC-4001", name: "Rani pink keychain" },
  { id: "DC-5001", name: "Mint baby booties" },
];

function matches(q) {
  return catalogue.filter((item) => item.name.toLowerCase().includes(String(q).toLowerCase()));
}

function reply(body) {
  return { ok: true, status: 200, json: async () => body };
}

/** The q in a request URL, or null. */
function queryOf(url) {
  return new URL(String(url), "https://dhaaga.in").searchParams.get("q");
}

/** A pretend search API that answers at once. */
function quickServer() {
  const fetchMock = vi.fn(async (url) => reply(matches(queryOf(url))));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** A pretend search API that answers only when the test calls answer(q). */
function controllableServer() {
  const waiting = new Map();
  const fetchMock = vi.fn(
    (url) =>
      new Promise((resolve) => {
        waiting.set(queryOf(url), resolve);
      }),
  );
  vi.stubGlobal("fetch", fetchMock);
  return {
    fetchMock,
    answer(q) {
      waiting.get(q)?.(reply(matches(q)));
    },
  };
}

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
  vi.unstubAllGlobals();
});

describe("createSearch", () => {
  it("waits for a 300 ms pause, then searches once", async () => {
    const fetchMock = quickServer();
    const show = vi.fn();
    const onType = createSearch(show);
    onType("g");
    await vi.advanceTimersByTimeAsync(100);
    onType("go");
    await vi.advanceTimersByTimeAsync(100);
    onType("gol");
    await vi.advanceTimersByTimeAsync(100);
    onType("golu");
    await vi.advanceTimersByTimeAsync(299);
    expect(fetchMock.mock.calls.length, "no request should go out while the customer is still typing").toBe(0);
    await vi.advanceTimersByTimeAsync(1);
    expect(fetchMock.mock.calls.length, "exactly one request should go out after the pause").toBe(1);
    expect(show.mock.calls, "show should get the names of the matching items").toEqual([[["Golu the elephant"]]]);
  });

  it("asks the catalogue endpoint with the text as q", async () => {
    const fetchMock = quickServer();
    createSearch(vi.fn())("golu");
    await vi.advanceTimersByTimeAsync(300);
    const url = new URL(String(fetchMock.mock.calls[0]?.[0] ?? ""), "https://dhaaga.in");
    expect(url.pathname, "the request should go to /api/catalogue").toBe("/api/catalogue");
    expect(url.searchParams.get("q"), "the query string should be q=golu").toBe("golu");
  });

  it("trims spaces from the text before searching", async () => {
    const fetchMock = quickServer();
    createSearch(vi.fn())("  golu  ");
    await vi.advanceTimersByTimeAsync(300);
    expect(queryOf(fetchMock.mock.calls[0]?.[0] ?? ""), "q should be \"golu\" without the spaces").toBe("golu");
  });

  it("searches for words with a space in them", async () => {
    quickServer();
    const show = vi.fn();
    createSearch(show)("rani pink");
    await vi.advanceTimersByTimeAsync(300);
    expect(show.mock.calls, "\"rani pink\" should find the Rani pink keychain; build q with URLSearchParams").toEqual([[["Rani pink keychain"]]]);
  });

  it("clears the results without asking the server when the box is empty", async () => {
    const fetchMock = quickServer();
    const show = vi.fn();
    createSearch(show)("   ");
    await vi.advanceTimersByTimeAsync(300);
    expect(fetchMock.mock.calls.length, "empty text shouldn't send a request").toBe(0);
    expect(show.mock.calls, "empty text should call show with an empty list").toEqual([[[]]]);
  });

  it("uses the pause it is given", async () => {
    const fetchMock = quickServer();
    createSearch(vi.fn(), 500)("bag");
    await vi.advanceTimersByTimeAsync(499);
    expect(fetchMock.mock.calls.length, "with ms = 500, nothing should go out after 499 ms").toBe(0);
    await vi.advanceTimersByTimeAsync(1);
    expect(fetchMock.mock.calls.length, "with ms = 500, the search should go out after 500 ms").toBe(1);
  });

  it("ignores an older search's answer that arrives late", async () => {
    const server = controllableServer();
    const show = vi.fn();
    const onType = createSearch(show);
    onType("golu");
    await vi.advanceTimersByTimeAsync(300);
    onType("bag");
    await vi.advanceTimersByTimeAsync(300);
    expect(server.fetchMock.mock.calls.length, "two searches with a pause between them should send two requests").toBe(2);
    server.answer("bag");
    await vi.advanceTimersByTimeAsync(0);
    server.answer("golu");
    await vi.advanceTimersByTimeAsync(0);
    expect(show.mock.calls, "only the newest search (bag) may show results; the late golu answer must be ignored").toEqual([
      [["Indigo tote bag"]],
    ]);
  });
});
