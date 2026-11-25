import { describe, it, expect, afterEach, vi } from "vitest";
import { getItems, getStock } from "../src/lib/api.js";
import { stubApi, ITEMS, STOCK, ITEMS_URL, STOCK_URL } from "./fakeApi.js";

afterEach(() => {
  vi.unstubAllGlobals();
});

describe("getItems", () => {
  it("asks the items API and gives back its list", async () => {
    const fetchMock = stubApi();
    const items = await getItems();
    expect(fetchMock.mock.calls[0]?.[0], `getItems should fetch ${ITEMS_URL}`).toBe(ITEMS_URL);
    expect(items, "getItems should return the JSON list the API sent").toEqual(ITEMS);
  });

  it("throws a clear error when the API answers with an error status", async () => {
    stubApi({ [ITEMS_URL]: [{ message: "down" }, 503] });
    await expect(getItems(), "a 503 answer should throw Items API answered 503 (check res.ok)").rejects.toThrow("Items API answered 503");
  });
});

describe("getStock", () => {
  it("asks the stock API and gives back its counts", async () => {
    const fetchMock = stubApi();
    const stock = await getStock();
    expect(fetchMock.mock.calls[0]?.[0], `getStock should fetch ${STOCK_URL}`).toBe(STOCK_URL);
    expect(stock, "getStock should return the JSON counts the API sent").toEqual(STOCK);
  });

  it("throws a clear error when the API answers with an error status", async () => {
    stubApi({ [STOCK_URL]: [{ message: "oops" }, 500] });
    await expect(getStock(), "a 500 answer should throw Stock API answered 500").rejects.toThrow("Stock API answered 500");
  });
});
