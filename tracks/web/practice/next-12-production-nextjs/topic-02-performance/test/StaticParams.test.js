import { describe, it, expect, vi } from "vitest";

vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
}));

import { generateStaticParams } from "../src/app/items/[id]/page.jsx";

describe("generateStaticParams", () => {
  it("lists every item in the catalogue, so each page is built ahead of time", async () => {
    const params = await generateStaticParams();
    expect(Array.isArray(params), "generateStaticParams should return an array").toBe(true);
    expect(params.length, "there are 5 items, so 5 pages should be built ahead of time (TODO 1)").toBe(5);
  });

  it("gives one { id } per item, in catalogue order", async () => {
    expect(
      await generateStaticParams(),
      "each entry should be { id } for one item, in the order listItems() gives them (TODO 1)"
    ).toEqual([{ id: "DC-1042" }, { id: "DC-1101" }, { id: "DC-1205" }, { id: "DC-1301" }, { id: "DC-1402" }]);
  });
});
