import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import ItemPage from "../src/app/items/[id]/page.jsx";

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

const props = (id) => ({ params: Promise.resolve({ id }), searchParams: Promise.resolve({}) });

describe("ItemPage (silver)", () => {
  it("calls notFound() for an id that isn't in the catalogue", async () => {
    await expect(ItemPage(props("DC-9999")), "/items/DC-9999 should call notFound(), not show another item").rejects.toThrow(
      "NEXT_NOT_FOUND",
    );
  });

  it("doesn't treat a cut-short id as a match", async () => {
    await expect(
      ItemPage(props("DC-104")),
      "/items/DC-104 is not Golu's id (DC-1042), so it should call notFound()",
    ).rejects.toThrow("NEXT_NOT_FOUND");
  });

  it("still shows Rani on Rani's own page", async () => {
    render(await ItemPage(props("DC-1038")));
    expect(
      screen.queryByRole("heading", { level: 1 })?.textContent,
      "/items/DC-1038 is Rani's real page, so it should show Rani the pink elephant",
    ).toBe("Rani the pink elephant");
  });
});
