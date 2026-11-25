import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import CataloguePage, { generateMetadata } from "../src/app/page.jsx";

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

/** Renders the home page for a set of search params, like { page: "2" }. */
async function show(params) {
  render(await CataloguePage({ searchParams: Promise.resolve(params) }));
}

function itemNames() {
  return screen.queryAllByRole("listitem").map((item) => item.querySelector("a")?.textContent);
}

function pageLink(name) {
  return screen.queryByRole("link", { name });
}

describe("Round 3: the paginated catalogue", () => {
  it("shows the first two items on page 1, linking to their pages", async () => {
    await show({});
    expect(itemNames(), "with no ?page, page 1 shows Golu and the coaster set (TODO 5)").toEqual(["Golu the elephant", "Marigold coaster set"]);
    expect(pageLink("Golu the elephant")?.getAttribute("href"), "Golu's name should link to /items/DC-1042").toBe("/items/DC-1042");
    expect(screen.queryByText("Page 1 of 3"), "the page should say Page 1 of 3").not.toBeNull();
  });

  it("reads the page from searchParams, which is a Promise", async () => {
    await show({ page: "2" });
    expect(itemNames(), "?page=2 should show the keychain and the tote bag. Await searchParams first (TODO 4)").toEqual(["Indigo keychain", "Tote bag"]);
    expect(screen.queryByText("Page 2 of 3"), "the page should say Page 2 of 3 (TODO 4)").not.toBeNull();
  });

  it("shows Previous and Next only where they lead somewhere", async () => {
    await show({});
    expect(pageLink("Previous"), "page 1 has no Previous link (TODO 5)").toBeNull();
    expect(pageLink("Next")?.getAttribute("href"), "page 1's Next goes to /?page=2 (TODO 5)").toBe("/?page=2");
    cleanup();

    await show({ page: "2" });
    expect(pageLink("Previous")?.getAttribute("href"), "page 2's Previous goes to /, the one address of page 1 (TODO 5)").toBe("/");
    expect(pageLink("Next")?.getAttribute("href"), "page 2's Next goes to /?page=3 (TODO 5)").toBe("/?page=3");
    cleanup();

    await show({ page: "3" });
    expect(itemNames(), "the last page shows only the baby booties (TODO 5)").toEqual(["Baby booties"]);
    expect(pageLink("Previous")?.getAttribute("href"), "page 3's Previous goes to /?page=2 (TODO 5)").toBe("/?page=2");
    expect(pageLink("Next"), "the last page has no Next link (TODO 5)").toBeNull();
  });

  it("sends ?page=1 to the home page's one address", async () => {
    await expect(CataloguePage({ searchParams: Promise.resolve({ page: "1" }) }), "?page=1 should redirect to / (TODO 4)").rejects.toThrow("NEXT_REDIRECT /");
  });

  it("shows a 404 for a page that doesn't exist", async () => {
    for (const page of ["0", "4", "abc", "2.5", ["2", "3"]]) {
      await expect(CataloguePage({ searchParams: Promise.resolve({ page }) }), `?page=${JSON.stringify(page)} should call notFound() (TODO 4)`).rejects.toThrow("NEXT_NOT_FOUND");
    }
  });

  it("gives each page its own title and canonical address", async () => {
    expect(await generateMetadata({ searchParams: Promise.resolve({}) }), "page 1's metadata (TODO 6)").toEqual({ title: "Catalogue", alternates: { canonical: "/" } });
    expect(await generateMetadata({ searchParams: Promise.resolve({ page: "3" }) }), "page 3's metadata (TODO 6)").toEqual({
      title: "Catalogue, page 3",
      alternates: { canonical: "/?page=3" },
    });
    expect(await generateMetadata({ searchParams: Promise.resolve({ page: "abc" }) }), "a bad page should be kept out of search results (TODO 6)").toEqual({
      title: "Page not found",
      robots: { index: false },
    });
  });
});
