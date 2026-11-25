import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import { NavBar } from "../src/app/NavBar.jsx";
import { source, withoutComments } from "./source.js";

// A stand-in for next/navigation: the tests choose the current address.
const nav = vi.hoisted(() => ({ pathname: "/", push: vi.fn() }));
vi.mock("next/navigation", () => ({
  usePathname: () => nav.pathname,
  useRouter: () => ({ push: nav.push, replace: vi.fn(), back: vi.fn() }),
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
  redirect: vi.fn((to) => {
    throw new Error("NEXT_REDIRECT " + to);
  }),
}));

afterEach(() => {
  cleanup();
  nav.pathname = "/";
});

function link(name) {
  return screen.queryByRole("link", { name });
}

describe("NavBar", () => {
  it("links to Home, Shop and Help", () => {
    render(<NavBar />);
    expect(link("Home")?.getAttribute("href"), "the Home link should go to /").toBe("/");
    expect(link("Shop")?.getAttribute("href"), "the Shop link should go to /items").toBe("/items");
    expect(link("Help")?.getAttribute("href"), "the Help link should go to /help").toBe("/help");
  });

  it("uses Link from next/link, not plain <a> tags", () => {
    const code = withoutComments(source("app/NavBar.jsx"));
    expect(/<Link[\s>]/.test(code), "NavBar should draw its links with <Link href=...>").toBe(true);
    expect(/<a[\s>]/.test(code), "NavBar should have no plain <a> tags left; Link renders the <a> for you").toBe(false);
  });

  it("marks Shop as the current page on an item's page", () => {
    nav.pathname = "/items/DC-1042";
    render(<NavBar />);
    expect(link("Shop")?.getAttribute("aria-current"), "on /items/DC-1042, the Shop link should have aria-current=\"page\"").toBe("page");
    expect(link("Home")?.getAttribute("aria-current"), "on /items/DC-1042, the Home link should have no aria-current").toBeNull();
    expect(link("Help")?.getAttribute("aria-current"), "on /items/DC-1042, the Help link should have no aria-current").toBeNull();
  });

  it("marks only Home as the current page on the home page", () => {
    nav.pathname = "/";
    render(<NavBar />);
    expect(link("Home")?.getAttribute("aria-current"), "on /, the Home link should have aria-current=\"page\"").toBe("page");
    expect(link("Shop")?.getAttribute("aria-current"), "on /, the Shop link should have no aria-current").toBeNull();
  });

  it("marks Help as the current page inside the help centre", () => {
    nav.pathname = "/help/delivery";
    render(<NavBar />);
    expect(link("Help")?.getAttribute("aria-current"), "on /help/delivery, the Help link should have aria-current=\"page\"").toBe("page");
    expect(link("Home")?.getAttribute("aria-current"), "on /help/delivery, the Home link should have no aria-current").toBeNull();
  });
});
