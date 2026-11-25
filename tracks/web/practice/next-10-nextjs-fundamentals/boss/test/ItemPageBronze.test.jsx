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

/** Renders the page the way Next.js 16 calls it: params is a Promise. */
async function open(id) {
  return render(await ItemPage({ params: Promise.resolve({ id }), searchParams: Promise.resolve({}) }));
}

const heading = () => screen.queryByRole("heading", { level: 1 })?.textContent;

describe("ItemPage (bronze)", () => {
  it("shows Golu on Golu's page", async () => {
    await open("DC-1042");
    expect(heading(), "/items/DC-1042 should show the <h1> Golu the elephant").toBe("Golu the elephant");
    expect(screen.queryByText("₹450.00"), "Golu's page should show his price, ₹450.00").not.toBeNull();
    expect(screen.queryByText("7 left"), "Golu's page should say 7 left").not.toBeNull();
  });

  it("shows the tote bag on the tote bag's page", async () => {
    await open("DC-1045");
    expect(heading(), "/items/DC-1045 should show the <h1> Indigo tote bag").toBe("Indigo tote bag");
    expect(screen.queryByText("₹899.00"), "the tote bag's page should show ₹899.00").not.toBeNull();
    expect(screen.queryByText("3 left"), "the tote bag's page should say 3 left").not.toBeNull();
  });

  it("says Sold out on the Mint keychain's page", async () => {
    await open("DC-1044");
    expect(heading(), "/items/DC-1044 should show the <h1> Mint keychain").toBe("Mint keychain");
    expect(screen.queryByText("Sold out"), "the Mint keychain has 0 left, so its page should say Sold out").not.toBeNull();
  });
});
