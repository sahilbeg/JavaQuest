import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { showToast } from "../src/timers.js";

let toast;

beforeEach(() => {
  vi.useFakeTimers();
  document.body.innerHTML = `<p id="toast" hidden></p>`;
  toast = document.querySelector("#toast");
});

afterEach(() => {
  vi.useRealTimers();
});

describe("showToast", () => {
  it("shows the message straight away", () => {
    showToast(toast, "Added Golu the elephant to your cart");
    expect(toast.textContent, "the toast should show the text it was given").toBe("Added Golu the elephant to your cart");
    expect(toast.hidden, "the toast should be visible (hidden = false) right after showToast").toBe(false);
  });

  it("hides the toast after 3 seconds by default", () => {
    showToast(toast, "Added a coaster set to your cart");
    vi.advanceTimersByTime(2999);
    expect(toast.hidden, "after 2.999 seconds the toast should still be visible").toBe(false);
    vi.advanceTimersByTime(1);
    expect(toast.hidden, "after 3 seconds the toast should be hidden").toBe(true);
  });

  it("uses the time it is given instead of 3 seconds", () => {
    showToast(toast, "Cart saved", 1000);
    vi.advanceTimersByTime(999);
    expect(toast.hidden, "with ms = 1000, the toast should still be visible after 0.999 seconds").toBe(false);
    vi.advanceTimersByTime(1);
    expect(toast.hidden, "with ms = 1000, the toast should hide after 1 second").toBe(true);
  });

  it("gives a second message its full 3 seconds, even if the first was still showing", () => {
    showToast(toast, "Added Golu the elephant to your cart");
    vi.advanceTimersByTime(2000);
    showToast(toast, "Added a tote bag to your cart");
    vi.advanceTimersByTime(2000);
    expect(toast.hidden, "the first toast's timer should have been cancelled, so the second message is still up 2 seconds later").toBe(false);
    expect(toast.textContent, "the toast should show the second message").toBe("Added a tote bag to your cart");
    vi.advanceTimersByTime(1000);
    expect(toast.hidden, "the second message should hide 3 seconds after it appeared").toBe(true);
  });

  it("leaves only one hide timer running at a time", () => {
    showToast(toast, "Added a keychain to your cart");
    showToast(toast, "Added baby booties to your cart");
    showToast(toast, "Added a coaster set to your cart");
    expect(vi.getTimerCount(), "three quick toasts should leave just one hide timer; cancel the old one with clearTimeout").toBe(1);
  });
});
