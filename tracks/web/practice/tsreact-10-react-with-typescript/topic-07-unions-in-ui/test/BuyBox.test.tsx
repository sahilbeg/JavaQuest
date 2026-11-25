import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { BuyBox } from "../src/BuyBox.tsx";

afterEach(cleanup);

function addButton() {
  return screen.queryByRole("button", { name: "Add to cart" }) as HTMLButtonElement | null;
}

describe("BuyBox", () => {
  it("lets you add an item that is in stock", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<BuyBox name="Marigold coaster set" stock={{ kind: "in-stock", count: 12 }} onAdd={onAdd} />);
    expect(addButton()?.disabled, "Add to cart should work for an item in stock").toBe(false);
    await user.click(addButton()!);
    expect(onAdd.mock.calls.length, "pressing Add to cart should call onAdd once").toBe(1);
  });

  it("disables Add to cart when the item is sold out", () => {
    render(<BuyBox name="Golu the elephant" stock={{ kind: "sold-out", backOn: null }} onAdd={() => {}} />);
    expect(addButton()?.disabled, "nobody should be able to add a sold-out Golu").toBe(true);
  });

  it("shows the note only for made-to-order items", () => {
    const { container } = render(<BuyBox name="Rani pink baby booties" stock={{ kind: "made-to-order", days: 5 }} onAdd={() => {}} />);
    expect(container.querySelector("p.note")?.textContent, "made-to-order booties should show the note").toBe("Crocheted for you after you order.");
    expect(addButton()?.disabled, "made-to-order items can still be added").toBe(false);
  });

  it("shows no note for an item in stock or sold out", () => {
    const { container } = render(
      <>
        <BuyBox name="Marigold coaster set" stock={{ kind: "in-stock", count: 12 }} onAdd={() => {}} />
        <BuyBox name="Golu the elephant" stock={{ kind: "sold-out", backOn: "14 Dec" }} onAdd={() => {}} />
      </>,
    );
    expect(container.querySelectorAll("p.note").length, "only made-to-order items have the note").toBe(0);
  });

  it("shows the right badge inside the box", () => {
    const { container } = render(<BuyBox name="Golu the elephant" stock={{ kind: "sold-out", backOn: "14 Dec" }} onAdd={() => {}} />);
    expect(container.querySelector(".buy-box p.badge")?.textContent, "the box should hold the sold-out badge").toBe("Sold out, back on 14 Dec");
  });
});
