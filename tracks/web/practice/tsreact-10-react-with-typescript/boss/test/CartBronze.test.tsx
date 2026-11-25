import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { Cart } from "../src/Cart.tsx";

afterEach(cleanup);

// Mrs. Deshpande's cart: one Golu, and two lantern keychains that were retired this morning.
function deshpandeCart() {
  return [
    { id: "DC-1042", qty: 1 },
    { id: "DC-1031", qty: 2 },
  ];
}

describe("INC-10 bronze: a missing item", () => {
  it("never shows undefined or NaN", () => {
    const { container } = render(<Cart initialLines={deshpandeCart()} />);
    const text = container.textContent ?? "";
    expect(text.includes("undefined"), "the cart should never show the word undefined").toBe(false);
    expect(text.includes("NaN"), "the cart should never show NaN").toBe(false);
  });

  it("says the missing item is no longer available", () => {
    const { container } = render(<Cart initialLines={deshpandeCart()} />);
    const missing = container.querySelector("li.missing .name");
    expect(missing, "the DC-1031 line should have the classes cart-line missing").not.toBeNull();
    expect(missing?.textContent, "it should say DC-1031 is no longer available").toBe("DC-1031 is no longer available");
  });

  it("leaves the missing item out of the subtotal and total", () => {
    const { container } = render(<Cart initialLines={deshpandeCart()} />);
    expect(container.querySelector(".subtotal")?.textContent, "only Golu counts: Subtotal: ₹450.00").toBe("Subtotal: ₹450.00");
    expect(container.querySelector(".total")?.textContent, "₹450.00 plus ₹60.00 delivery is ₹510.00").toBe("Total: ₹510.00");
  });
});
