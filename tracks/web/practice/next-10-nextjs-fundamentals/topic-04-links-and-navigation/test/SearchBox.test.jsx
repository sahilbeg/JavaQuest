import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { SearchBox } from "../src/app/SearchBox.jsx";

// A stand-in for next/navigation that records every router.push.
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
  nav.push.mockClear();
});

async function search(text) {
  const user = userEvent.setup();
  render(<SearchBox />);
  const box = screen.queryByLabelText("Search the shop");
  expect(box, "the search box should keep its label Search the shop").not.toBeNull();
  if (text) await user.type(box, text);
  await user.click(screen.getByRole("button", { name: "Search" }));
}

describe("SearchBox", () => {
  it("goes to the results for what was typed", async () => {
    await search("golu");
    expect(nav.push.mock.calls.length, "pressing Search should call router.push once").toBe(1);
    expect(nav.push.mock.calls[0]?.[0], "searching for golu should go to /items?q=golu").toBe("/items?q=golu");
  });

  it("encodes spaces in the address", async () => {
    await search("Golu elephant");
    expect(nav.push.mock.calls[0]?.[0], "searching for Golu elephant should go to /items?q=Golu%20elephant").toBe("/items?q=Golu%20elephant");
  });

  it("trims spaces around what was typed", async () => {
    await search("  tote  ");
    expect(nav.push.mock.calls[0]?.[0], "searching for \"  tote  \" should go to /items?q=tote").toBe("/items?q=tote");
  });

  it("goes to the whole shop when nothing was typed", async () => {
    await search("   ");
    expect(nav.push.mock.calls.length, "pressing Search with an empty box should still call router.push once").toBe(1);
    expect(nav.push.mock.calls[0]?.[0], "an empty search should go to /items").toBe("/items");
  });
});
