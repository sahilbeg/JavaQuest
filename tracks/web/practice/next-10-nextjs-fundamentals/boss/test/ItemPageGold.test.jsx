import { describe, it, expect, vi } from "vitest";
import { generateMetadata } from "../src/app/items/[id]/page.jsx";

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

/** Calls generateMetadata the way Next.js 16 does. A throw becomes { threw } so the check can say so kindly. */
async function metadataFor(id) {
  return generateMetadata({ params: Promise.resolve({ id }), searchParams: Promise.resolve({}) }).catch((error) => ({
    threw: String(error),
  }));
}

describe("generateMetadata (gold)", () => {
  it("titles Golu's page with his name only", async () => {
    const meta = await metadataFor("DC-1042");
    expect(
      meta.title,
      "the title should be just Golu the elephant: the root layout's template already adds | Dhaaga & Co.",
    ).toBe("Golu the elephant");
  });

  it("describes the right item, with its price", async () => {
    const meta = await metadataFor("DC-1042");
    expect(meta.description, "the description should be Golu the elephant, handmade in Pune. ₹450.00.").toBe(
      "Golu the elephant, handmade in Pune. ₹450.00.",
    );
  });

  it("fills in the WhatsApp preview for the item's own address", async () => {
    const meta = await metadataFor("DC-1045");
    expect(meta.openGraph, "openGraph should have the tote bag's title and description, and url /items/DC-1045").toEqual({
      title: "Indigo tote bag",
      description: "Indigo tote bag, handmade in Pune. ₹899.00.",
      url: "/items/DC-1045",
    });
  });

  it("gives an unknown id the title Item not found, without throwing", async () => {
    const meta = await metadataFor("DC-9999");
    expect(meta.threw, "generateMetadata should not throw for an unknown id").toBeUndefined();
    expect(meta, "an unknown id should get { title: \"Item not found\" }").toEqual({ title: "Item not found" });
  });
});
