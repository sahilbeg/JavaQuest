import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup, act } from "@testing-library/react";
import { App } from "../src/App.tsx";

beforeEach(() => {
  window.location.hash = "";
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

/** Changes the address like a link or the Back button would, and tells the page about it. */
function goTo(hash: string) {
  act(() => {
    window.location.hash = hash;
    window.dispatchEvent(new HashChangeEvent("hashchange"));
  });
}

/** The text of the page's main heading. */
function heading() {
  return screen.queryByRole("heading", { level: 1 })?.textContent;
}

describe("The store's pages", () => {
  it("shows the shop when the address has no hash", () => {
    render(<App />);
    expect(heading(), "with no hash, the <h1> should be All items").toBe("All items");
  });

  it("opens an item's page when the page loads at its address", () => {
    window.location.hash = "#/item/DC-1042";
    render(<App />);
    expect(heading(), "#/item/DC-1042 should show Golu the elephant").toBe("Golu the elephant");
    expect(screen.queryByText("₹450.00"), "Golu's page should show his price").not.toBeNull();
  });

  it("opens an order's page", () => {
    window.location.hash = "#/order/order-17";
    render(<App />);
    expect(heading(), "#/order/order-17 should show Order order-17").toBe("Order order-17");
  });

  it("shows Page not found for an address it doesn't know", () => {
    window.location.hash = "#/about";
    render(<App />);
    expect(heading(), "#/about should show Page not found").toBe("Page not found");
    expect(screen.queryByText("Nothing lives at /about."), "the page should say which path it couldn't find").not.toBeNull();
  });

  it("changes page when the hash changes", () => {
    render(<App />);
    goTo("#/item/DC-2001");
    expect(heading(), "after the hash changes to #/item/DC-2001 the page should be Marigold coaster set (listen for hashchange)").toBe(
      "Marigold coaster set",
    );
    goTo("#/order/order-18");
    expect(heading(), "after the hash changes to #/order/order-18 the page should be Order order-18").toBe("Order order-18");
    goTo("#/");
    expect(heading(), "going back to #/ should show the shop again").toBe("All items");
  });

  it("gives every link a hash address built from its route", () => {
    render(<App />);
    expect(screen.queryByRole("link", { name: "Shop" })?.getAttribute("href"), "the Shop link should point to #/").toBe("#/");
    expect(screen.queryByRole("link", { name: "My order" })?.getAttribute("href"), "My order should point to #/order/order-17").toBe(
      "#/order/order-17",
    );
    expect(
      screen.queryByRole("link", { name: "Golu the elephant" })?.getAttribute("href"),
      "Golu's link should point to #/item/DC-1042",
    ).toBe("#/item/DC-1042");
  });

  it("stops listening for hashchange when the app is removed", () => {
    const removed = vi.spyOn(window, "removeEventListener");
    const { unmount } = render(<App />);
    unmount();
    const calls = removed.mock.calls.filter(([type]) => type === "hashchange");
    expect(calls.length > 0, "the cleanup should call window.removeEventListener(\"hashchange\", ...)").toBe(true);
  });
});
