import { describe, it, expect, vi } from "vitest";

vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
}));

import { generateMetadata } from "../src/app/items/[id]/page.jsx";

const GOLU_DESCRIPTION = "A small crocheted elephant in Indigo and Marigold, modelled on our Golu.";

async function metadataFor(id) {
  return generateMetadata({ params: Promise.resolve({ id }) });
}

describe("generateMetadata for an item page", () => {
  it("uses the item's name as the title and its description", async () => {
    const golu = await metadataFor("DC-1042");
    expect(golu.title, "the title should be just the item's name; the layout's template adds \" | Dhaaga & Co.\" (TODO 3)").toBe("Golu the elephant");
    expect(golu.description, "the description should be the item's own description (TODO 3)").toBe(GOLU_DESCRIPTION);
  });

  it("names one canonical address for the page", async () => {
    const golu = await metadataFor("DC-1042");
    expect(golu.alternates, "alternates should be { canonical: \"/items/DC-1042\" } (TODO 3)").toEqual({ canonical: "/items/DC-1042" });
  });

  it("gives WhatsApp and Instagram a preview with a described photo", async () => {
    const golu = await metadataFor("DC-1042");
    expect(golu.openGraph, "openGraph should have the title, the description and one 1200 by 630 image with alt text (TODO 3)").toEqual({
      title: "Golu the elephant",
      description: GOLU_DESCRIPTION,
      images: [{ url: "/images/DC-1042.jpg", width: 1200, height: 630, alt: "A small Indigo crocheted elephant with Marigold ears" }],
    });
  });

  it("keeps an unknown item out of search results", async () => {
    expect(await metadataFor("DC-9999"), "an unknown item should give { title: \"Item not found\", robots: { index: false } } (TODO 3)").toEqual({
      title: "Item not found",
      robots: { index: false },
    });
  });
});
