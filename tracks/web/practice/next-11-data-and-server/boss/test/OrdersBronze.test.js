import { describe, it, expect, beforeEach } from "vitest";
import { createOrder } from "../src/lib/orders.js";
import { resetDb, allOrders, findOrder } from "../src/lib/db.js";
import { ASHA, RAVI } from "./customers.js";

beforeEach(() => {
  resetDb();
});

describe("Bronze: two customers at the same moment", () => {
  it("gives two customers who order at the same moment different order numbers", async () => {
    const [asha, ravi] = await Promise.all([createOrder(ASHA), createOrder(RAVI)]);
    expect(asha.id === ravi.id, `Asha and Ravi both got ${asha.id}: two orders at once must never share a number`).toBe(false);
  });

  it("saves both orders", async () => {
    const [asha, ravi] = await Promise.all([createOrder(ASHA), createOrder(RAVI)]);
    expect((await allOrders()).length, "3 old orders plus 2 new ones should make 5 (one was overwritten)").toBe(5);
    expect((await findOrder(asha.id))?.name, `${asha.id} should be Asha's order`).toBe("Asha Rao");
    expect((await findOrder(ravi.id))?.name, `${ravi.id} should be Ravi's order`).toBe("Ravi Joshi");
  });

  it("still works out each order's total", async () => {
    const [asha, ravi] = await Promise.all([createOrder(ASHA), createOrder(RAVI)]);
    expect(asha.totalPaise, "Golu is 45000 paise plus 6000 delivery").toBe(51000);
    expect(ravi.totalPaise, "two pairs of booties are 70000 paise plus 6000 delivery").toBe(76000);
  });
});
