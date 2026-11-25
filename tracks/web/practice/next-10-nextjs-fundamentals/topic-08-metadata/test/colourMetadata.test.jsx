import { describe, it, expect, vi } from "vitest";
import { describeColour, generateMetadata } from "../src/app/colours/[colour]/page.jsx";

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

/** Calls generateMetadata the way Next.js 16 does, with params as a Promise. */
async function metadataFor(colour) {
  return generateMetadata({ params: Promise.resolve({ colour }), searchParams: Promise.resolve({}) }).catch((error) => ({
    threw: String(error),
  }));
}

const golu = { id: "DC-1042", paise: 45000 };
const tote = { id: "DC-1045", paise: 89900 };

describe("describeColour", () => {
  it("counts the pieces and gives the cheapest price", () => {
    expect(describeColour("Indigo", [tote, golu]), "two items, the cheapest ₹450.00").toBe(
      "2 handmade pieces in Indigo, from ₹450.00.",
    );
  });

  it("says piece, not pieces, for one item", () => {
    expect(describeColour("Marigold", [{ id: "DC-1043", paise: 15000 }]), "one item should say 1 handmade piece").toBe(
      "1 handmade piece in Marigold, from ₹150.00.",
    );
  });

  it("says so when there are no pieces", () => {
    expect(describeColour("Off-white", []), "no items should say No pieces in Off-white right now.").toBe(
      "No pieces in Off-white right now.",
    );
  });
});

describe("generateMetadata for a colour page", () => {
  it("titles the page with the colour, leaving the shop's name to the template", async () => {
    const meta = await metadataFor("indigo");
    expect(meta.title, "the title should be just Indigo crochet: the layout's template adds | Dhaaga & Co.").toBe("Indigo crochet");
  });

  it("describes what's in the colour, using the catalogue", async () => {
    const indigo = await metadataFor("indigo");
    expect(indigo.description, "Indigo has Golu (₹450.00) and the tote bag (₹899.00)").toBe("2 handmade pieces in Indigo, from ₹450.00.");
    const pink = await metadataFor("rani-pink");
    expect(pink.description, "Rani pink's cheapest piece is the ₹350.00 booties").toBe("2 handmade pieces in Rani pink, from ₹350.00.");
    const offWhite = await metadataFor("off-white");
    expect(offWhite.description, "Off-white has nothing in the catalogue").toBe("No pieces in Off-white right now.");
  });

  it("fills in the preview card", async () => {
    const meta = await metadataFor("indigo");
    expect(meta.openGraph, "openGraph should repeat the title and description, with url /colours/indigo").toEqual({
      title: "Indigo crochet",
      description: "2 handmade pieces in Indigo, from ₹450.00.",
      url: "/colours/indigo",
    });
  });

  it("gives an unknown colour the title Colour not found", async () => {
    const meta = await metadataFor("purple");
    expect(meta.threw, "generateMetadata should not throw for an unknown colour").toBeUndefined();
    expect(meta, "an unknown colour should get { title: \"Colour not found\" }").toEqual({ title: "Colour not found" });
  });
});
