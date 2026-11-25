import { describe, it, expect, afterEach, vi } from "vitest";
import { getCatalogue, getStock, getShopHours } from "../src/lib/api.js";

/** A pretend fetch that always answers { fine: true }, and remembers what it was asked. */
function stubFetch() {
  const fetchMock = vi.fn(async () => ({ ok: true, status: 200, json: async () => ({ fine: true }) }));
  vi.stubGlobal("fetch", fetchMock);
  return fetchMock;
}

/** The options object the code passed to fetch, or {} when it passed none. */
function optionsOf(fetchMock) {
  return fetchMock.mock.calls[0]?.[1] ?? {};
}

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("getStock", () => {
  it("never keeps a copy of the stock", async () => {
    const fetchMock = stubFetch();
    expect(await getStock(), "getStock should still return the API's JSON").toEqual({ fine: true });
    expect(fetchMock.mock.calls[0]?.[0], "getStock should fetch https://api.dhaaga.in/stock").toBe("https://api.dhaaga.in/stock");
    const options = optionsOf(fetchMock);
    expect(options.cache, 'stock must be fresh on every request, so use { cache: "no-store" }').toBe("no-store");
    expect(options.next?.revalidate, "stock shouldn't have a revalidate time either").toBeUndefined();
  });
});

describe("getCatalogue", () => {
  it("keeps the catalogue for an hour at most", async () => {
    const fetchMock = stubFetch();
    expect(await getCatalogue(), "getCatalogue should still return the API's JSON").toEqual({ fine: true });
    const options = optionsOf(fetchMock);
    expect(options.next?.revalidate, "the catalogue should be cached for 3600 seconds: { next: { revalidate: 3600 } }").toBe(3600);
    expect(options.cache, "a cached catalogue must not say no-store").not.toBe("no-store");
  });

  it("tags the catalogue so a price change can clear it", async () => {
    const fetchMock = stubFetch();
    await getCatalogue();
    expect(optionsOf(fetchMock).next?.tags, 'the catalogue should be tagged: next: { tags: ["catalogue"] }').toEqual(["catalogue"]);
  });
});

describe("getShopHours", () => {
  it("keeps the hours until told they changed, tagged hours", async () => {
    const fetchMock = stubFetch();
    expect(await getShopHours(), "getShopHours should still return the API's JSON").toEqual({ fine: true });
    const options = optionsOf(fetchMock);
    expect(options.cache, 'the hours should be kept with { cache: "force-cache" }').toBe("force-cache");
    expect(options.next?.tags, 'the hours should be tagged: next: { tags: ["hours"] }').toEqual(["hours"]);
  });
});
