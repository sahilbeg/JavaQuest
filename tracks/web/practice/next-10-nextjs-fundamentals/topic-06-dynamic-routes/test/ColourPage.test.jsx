import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import ColourPage, { generateStaticParams } from "../src/app/colours/[colour]/page.jsx";

// A stand-in for next/navigation: notFound() throws, as the real one does.
vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
  redirect: vi.fn((to) => {
    throw new Error("NEXT_REDIRECT " + to);
  }),
  useRouter: () => ({ push: vi.fn() }),
  usePathname: () => "/",
}));

afterEach(cleanup);

/** Renders the page the way Next.js 16 calls it: params and searchParams are Promises. */
async function open(colour, search = {}) {
  return render(await ColourPage({ params: Promise.resolve({ colour }), searchParams: Promise.resolve(search) }));
}

const names = (container) => [...container.querySelectorAll("li a")].map((a) => a.textContent);

describe("ColourPage", () => {
  it("lists one colour page per colour for the build", () => {
    expect(generateStaticParams(), "generateStaticParams should return one { colour: slug } per colour, in COLOURS order").toEqual([
      { colour: "marigold" },
      { colour: "indigo" },
      { colour: "rani-pink" },
      { colour: "mint" },
      { colour: "off-white" },
    ]);
  });

  it("names the colour in the heading", async () => {
    await open("indigo");
    expect(screen.queryByRole("heading", { level: 1 })?.textContent, "/colours/indigo should have the <h1> Indigo").toBe("Indigo");
  });

  it("lists only the items in that colour, each linking to its own page", async () => {
    const { container } = await open("indigo");
    expect(names(container), "/colours/indigo should list Golu the elephant and the Indigo tote bag").toEqual([
      "Golu the elephant",
      "Indigo tote bag",
    ]);
    const hrefs = [...container.querySelectorAll("li a")].map((a) => a.getAttribute("href"));
    expect(hrefs, "each item should link to /items/<its id>").toEqual(["/items/DC-1042", "/items/DC-1045"]);
    expect(container.querySelector("li")?.textContent, "Golu's row should read Golu the elephant ₹450.00").toBe("Golu the elephant ₹450.00");
  });

  it("keeps catalogue order when no sort is asked for", async () => {
    const { container } = await open("rani-pink");
    expect(names(container), "without ?sort, Rani pink items stay in catalogue order").toEqual([
      "Rani the pink elephant",
      "Rani pink baby booties",
    ]);
  });

  it("puts the cheapest first with ?sort=price", async () => {
    const { container } = await open("rani-pink", { sort: "price" });
    expect(names(container), "with ?sort=price, the ₹350.00 booties should come before the ₹450.00 elephant").toEqual([
      "Rani pink baby booties",
      "Rani the pink elephant",
    ]);
  });

  it("links to the cheapest-first version of the page", async () => {
    await open("rani-pink");
    const link = screen.queryByRole("link", { name: "Cheapest first" });
    expect(link?.getAttribute("href"), "the Cheapest first link should go to /colours/rani-pink?sort=price").toBe("/colours/rani-pink?sort=price");
  });

  it("says so when a colour has no items", async () => {
    const { container } = await open("off-white");
    expect(screen.queryByText("Nothing in Off-white right now."), "/colours/off-white should say Nothing in Off-white right now.").not.toBeNull();
    expect(container.querySelectorAll("li").length, "an empty colour should show no list items").toBe(0);
  });

  it("calls notFound() for a colour that doesn't exist", async () => {
    await expect(
      ColourPage({ params: Promise.resolve({ colour: "purple" }), searchParams: Promise.resolve({}) }),
      "/colours/purple should call notFound(), so the 404 page shows",
    ).rejects.toThrow("NEXT_NOT_FOUND");
  });
});
