import { describe, it, expect, afterEach, beforeEach, vi } from "vitest";
import { render, screen, cleanup, act } from "@testing-library/react";
import { App } from "../src/App.jsx";

beforeEach(() => {
  window.location.hash = "";
});

afterEach(() => {
  cleanup();
  vi.restoreAllMocks();
});

/** Changes the address like a link or the Back button would, and tells the page about it. */
function goTo(hash) {
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
  it("shows the home page when the address has no hash", () => {
    render(<App />);
    expect(heading(), "with no hash, the <h1> should be Handmade in Pune").toBe("Handmade in Pune");
  });

  it("opens the cart page when the page loads at #/cart", () => {
    window.location.hash = "#/cart";
    render(<App />);
    expect(heading(), "loading at #/cart should show the Your cart page").toBe("Your cart");
  });

  it("shows an item's page from the id in the address", () => {
    window.location.hash = "#/item/DC-1042";
    render(<App />);
    expect(heading(), "#/item/DC-1042 should show Golu the elephant").toBe("Golu the elephant");
  });

  it("shows Page not found for an address it doesn't know", () => {
    window.location.hash = "#/about";
    render(<App />);
    expect(heading(), "#/about should show Page not found").toBe("Page not found");
  });

  it("changes page when the hash changes", () => {
    render(<App />);
    goTo("#/cart");
    expect(heading(), "after the hash changes to #/cart the page should be Your cart (listen for hashchange)").toBe("Your cart");
    goTo("#/item/DC-2001");
    expect(heading(), "after the hash changes to #/item/DC-2001 the page should be Marigold coaster set").toBe(
      "Marigold coaster set",
    );
    goTo("#/");
    expect(heading(), "going back to #/ should show the home page again").toBe("Handmade in Pune");
  });

  it("gives every link a hash address", () => {
    render(<App />);
    expect(screen.queryByRole("link", { name: "Cart" })?.getAttribute("href"), "the Cart link should point to #/cart").toBe("#/cart");
    expect(screen.queryByRole("link", { name: "Home" })?.getAttribute("href"), "the Home link should point to #/").toBe("#/");
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
