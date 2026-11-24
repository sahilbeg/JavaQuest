import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { debounce } from "../src/search.js";

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
});

describe("debounce", () => {
  it("waits until the calls stop, then runs once with the last arguments", () => {
    const fn = vi.fn();
    const debounced = debounce(fn, 300);
    debounced("g");
    vi.advanceTimersByTime(100);
    debounced("go");
    vi.advanceTimersByTime(100);
    debounced("golu");
    vi.advanceTimersByTime(299);
    expect(fn.mock.calls.length, "nothing should run until 300 ms after the last call").toBe(0);
    vi.advanceTimersByTime(1);
    expect(fn.mock.calls, "fn should run once, with \"golu\" from the last call").toEqual([["golu"]]);
  });

  it("doesn't run straight away", () => {
    const fn = vi.fn();
    debounce(fn, 300)("golu");
    expect(fn.mock.calls.length, "a debounced call should wait, not run at once").toBe(0);
  });

  it("passes on every argument", () => {
    const fn = vi.fn();
    const debounced = debounce(fn, 100);
    debounced("DC-1042", 2, "Marigold");
    vi.advanceTimersByTime(100);
    expect(fn.mock.calls, "all three arguments should reach fn; use fn(...args)").toEqual([["DC-1042", 2, "Marigold"]]);
  });

  it("runs once per burst when there is a pause between bursts", () => {
    const fn = vi.fn();
    const debounced = debounce(fn, 300);
    debounced("g");
    debounced("golu");
    vi.advanceTimersByTime(1000);
    debounced("b");
    debounced("bag");
    vi.advanceTimersByTime(300);
    expect(fn.mock.calls, "two bursts with a pause between them should give two calls").toEqual([["golu"], ["bag"]]);
  });

  it("gives each debounced function its own timer", () => {
    const search = vi.fn();
    const save = vi.fn();
    const debouncedSearch = debounce(search, 300);
    const debouncedSave = debounce(save, 300);
    debouncedSearch("golu");
    debouncedSave("draft");
    vi.advanceTimersByTime(300);
    expect(search.mock.calls, "the search should still run; keep timer inside debounce, not at the top of the file").toEqual([["golu"]]);
    expect(save.mock.calls, "the save should run too").toEqual([["draft"]]);
  });
});
