import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { startCountdown } from "../src/timers.js";

beforeEach(() => {
  vi.useFakeTimers();
});

afterEach(() => {
  vi.useRealTimers();
});

describe("startCountdown", () => {
  it("calls onTick with the seconds left, once every second", () => {
    const onTick = vi.fn();
    const onDone = vi.fn();
    startCountdown(3, onTick, onDone);
    expect(onTick.mock.calls.length, "nothing should tick before the first second has passed").toBe(0);
    vi.advanceTimersByTime(1000);
    expect(onTick.mock.calls, "after 1 second, onTick should have been called once with 2").toEqual([[2]]);
    vi.advanceTimersByTime(1000);
    expect(onTick.mock.calls, "after 2 seconds, onTick should have been called with 2 and then 1").toEqual([[2], [1]]);
    expect(onDone.mock.calls.length, "onDone should not run while seconds are left").toBe(0);
  });

  it("calls onDone once when it reaches 0, without ticking 0", () => {
    const onTick = vi.fn();
    const onDone = vi.fn();
    startCountdown(3, onTick, onDone);
    vi.advanceTimersByTime(3000);
    expect(onDone.mock.calls.length, "after 3 seconds, onDone should have been called once").toBe(1);
    expect(onTick.mock.calls, "onTick should get 2 and 1, but not 0").toEqual([[2], [1]]);
  });

  it("stops by itself after reaching 0", () => {
    const onTick = vi.fn();
    const onDone = vi.fn();
    startCountdown(3, onTick, onDone);
    vi.advanceTimersByTime(10000);
    expect(onDone.mock.calls.length, "onDone should run only once, even 10 seconds later").toBe(1);
    expect(onTick.mock.calls.length, "onTick should not keep running after 0").toBe(2);
    expect(vi.getTimerCount(), "the interval should be cleared with clearInterval when it reaches 0").toBe(0);
  });

  it("counts down a 5 minute UPI QR", () => {
    const onTick = vi.fn();
    const onDone = vi.fn();
    startCountdown(300, onTick, onDone);
    vi.advanceTimersByTime(299000);
    expect(onTick.mock.calls.length, "a 300 second countdown should tick 299 times").toBe(299);
    expect(onTick.mock.calls[298], "the last tick should say 1 second is left").toEqual([1]);
    expect(onDone.mock.calls.length, "onDone should not run before 300 seconds").toBe(0);
    vi.advanceTimersByTime(1000);
    expect(onDone.mock.calls.length, "onDone should run at 300 seconds").toBe(1);
  });

  it("returns a function that stops the countdown early", () => {
    const onTick = vi.fn();
    const onDone = vi.fn();
    const stop = startCountdown(300, onTick, onDone);
    vi.advanceTimersByTime(2000);
    expect(onTick.mock.calls, "after 2 seconds, onTick should have been called with 299 and 298").toEqual([[299], [298]]);
    stop();
    vi.advanceTimersByTime(10000);
    expect(onTick.mock.calls.length, "after stop(), onTick should never run again").toBe(2);
    expect(onDone.mock.calls.length, "a stopped countdown should never call onDone").toBe(0);
  });
});
