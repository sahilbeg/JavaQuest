import { describe, it, expect, afterEach } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { OrderCard } from "../src/OrderCard.tsx";
import { makeOrder, makeSpy, STATUS_CASES } from "../src/fixtures.ts";
import type { OrderStatus } from "../src/types.ts";

afterEach(cleanup);

const STATUSES: OrderStatus[] = ["placed", "packed", "shipped", "delivered", "cancelled"];

function renderCard(status: OrderStatus) {
  const cancel = makeSpy<[orderId: string]>();
  const track = makeSpy<[orderId: string]>();
  render(<OrderCard order={makeOrder({ status })} onCancel={cancel.fn} onTrack={track.fn} />);
  return { cancel, track };
}

/** Finds a button by its name, with a friendly message when it is missing. */
function button(name: string, why: string) {
  const found = screen.queryByRole("button", { name });
  expect(found, why).not.toBeNull();
  return found as HTMLElement;
}

describe("The status table", () => {
  it("matches Sana's design for every status", () => {
    expect(STATUS_CASES, "STATUS_CASES should hold the label and buttons from the README's table, one row per status (TODO 2)").toEqual({
      placed: { label: "Order placed", canCancel: true, canTrack: false },
      packed: { label: "Packed, waiting for the courier", canCancel: true, canTrack: false },
      shipped: { label: "On its way", canCancel: false, canTrack: true },
      delivered: { label: "Delivered", canCancel: false, canTrack: false },
      cancelled: { label: "Cancelled", canCancel: false, canTrack: false },
    });
  });
});

describe("OrderCard, one check per status", () => {
  for (const status of STATUSES) {
    it(`shows the right label and buttons for a ${status} order`, () => {
      renderCard(status);
      const row = STATUS_CASES[status];
      const shown = screen.queryByRole("status")?.textContent;
      expect(shown, `the card says "${shown}" for a ${status} order, but STATUS_CASES.${status}.label says "${row.label}" (TODO 1 and TODO 2)`).toBe(row.label);
      expect(screen.queryByRole("button", { name: "Cancel order" }) !== null, `Cancel order should show for a ${status} order only if STATUS_CASES says canCancel`).toBe(row.canCancel);
      expect(screen.queryByRole("button", { name: "Track parcel" }) !== null, `Track parcel should show for a ${status} order only if STATUS_CASES says canTrack`).toBe(row.canTrack);
    });
  }
});

describe("OrderCard's buttons", () => {
  it("cancels a placed order once, with its id", async () => {
    const user = userEvent.setup();
    const { cancel, track } = renderCard("placed");
    await user.click(button("Cancel order", "a placed order should have a Cancel order button"));
    expect(cancel.calls, "one click on Cancel order should call onCancel once, with \"order-17\" (TODO 1 and TODO 3)").toEqual([["order-17"]]);
    expect(track.calls, "cancelling must not track the parcel").toEqual([]);
  });

  it("tracks a shipped order", async () => {
    const user = userEvent.setup();
    const { cancel, track } = renderCard("shipped");
    await user.click(button("Track parcel", "makeOrder({ status: \"shipped\" }) should make a shipped order, which has a Track parcel button (TODO 1)"));
    expect(track.calls, "one click on Track parcel should call onTrack once, with \"order-17\" (TODO 3)").toEqual([["order-17"]]);
    expect(cancel.calls, "a shipped order can't be cancelled").toEqual([]);
  });

  it("shows the total of every line", () => {
    const lines = [
      { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 2 },
      { id: "DC-1301", name: "Rani pink tote bag", paise: 89900, qty: 1 },
    ];
    render(<OrderCard order={makeOrder({ lines })} onCancel={makeSpy<[string]>().fn} onTrack={makeSpy<[string]>().fn} />);
    expect(screen.queryByText("Total: ₹1,799.00"), "two Golus and a tote bag cost 90000 + 89900 = 179900 paise, \"Total: ₹1,799.00\" (TODO 1)").not.toBeNull();
  });
});
