import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { Filters } from "../src/app/items/Filters.jsx";

// A stand-in for next/navigation that records every router.push.
const nav = vi.hoisted(() => ({ push: vi.fn() }));
vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
  redirect: vi.fn((to) => {
    throw new Error("NEXT_REDIRECT " + to);
  }),
  useRouter: () => ({ push: nav.push, replace: vi.fn(), back: vi.fn() }),
  usePathname: () => "/items",
}));

afterEach(() => {
  cleanup();
  nav.push.mockClear();
});

const pushed = () => nav.push.mock.calls.map((call) => call[0]);

describe("Filters", () => {
  it("goes to the chosen colour, keeping the search and the sort", async () => {
    const user = userEvent.setup();
    render(<Filters colour="" q="golu" sort="price-asc" />);
    await user.selectOptions(screen.getByLabelText("Colour"), "mint");
    expect(pushed(), "picking Mint should push /items?colour=mint&q=golu&sort=price-asc").toEqual([
      "/items?colour=mint&q=golu&sort=price-asc",
    ]);
  });

  it("goes back to all colours", async () => {
    const user = userEvent.setup();
    render(<Filters colour="indigo" q="" sort="" />);
    await user.selectOptions(screen.getByLabelText("Colour"), "");
    expect(pushed(), "picking All colours should push /items").toEqual(["/items"]);
  });

  it("sorts, keeping the colour", async () => {
    const user = userEvent.setup();
    render(<Filters colour="indigo" q="" sort="" />);
    await user.selectOptions(screen.getByLabelText("Sort"), "price-asc");
    expect(pushed(), "Cheapest first on Indigo should push /items?colour=indigo&sort=price-asc").toEqual([
      "/items?colour=indigo&sort=price-asc",
    ]);
  });

  it("searches for the trimmed text, keeping the colour", async () => {
    const user = userEvent.setup();
    render(<Filters colour="indigo" q="" sort="" />);
    await user.type(screen.getByLabelText("Search"), "  elephant ");
    await user.click(screen.getByRole("button", { name: "Search" }));
    expect(pushed(), "searching \"  elephant \" on Indigo should push /items?colour=indigo&q=elephant").toEqual([
      "/items?colour=indigo&q=elephant",
    ]);
  });
});
