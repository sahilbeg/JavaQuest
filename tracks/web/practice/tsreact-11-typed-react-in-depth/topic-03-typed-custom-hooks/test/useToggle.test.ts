import { describe, it, expect, afterEach } from "vitest";
import { renderHook, act, cleanup } from "@testing-library/react";
import { useToggle } from "../src/useToggle.ts";

afterEach(cleanup);

describe("useToggle", () => {
  it("starts off by default", () => {
    const { result } = renderHook(() => useToggle());
    expect(result.current[0], "useToggle() with no argument should start at false").toBe(false);
  });

  it("starts from the value you give it", () => {
    const { result } = renderHook(() => useToggle(true));
    expect(result.current[0], "useToggle(true) should start at true").toBe(true);
  });

  it("flips the value each time you call toggle", () => {
    const { result } = renderHook(() => useToggle());
    act(() => result.current[1]());
    expect(result.current[0], "after one toggle the value should be true").toBe(true);
    act(() => result.current[1]());
    expect(result.current[0], "after a second toggle the value should be false again").toBe(false);
  });

  it("flips twice when toggle is called twice in one go", () => {
    const { result } = renderHook(() => useToggle());
    act(() => {
      result.current[1]();
      result.current[1]();
    });
    expect(result.current[0], "two toggles in one event should end where they started (use the updater form)").toBe(false);
  });
});
