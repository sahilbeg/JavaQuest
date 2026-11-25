import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { Cart } from "../src/Cart.tsx";

afterEach(cleanup);

// A cart from a broken share link: two odd ids around one real tote bag.
function brokenLinkCart() {
  return [
    { id: "toString", qty: 1 },
    { id: "DC-1045", qty: 1 },
    { id: "constructor", qty: 1 },
  ];
}

describe("INC-10 gold: ids that only look like items", () => {
  it("treats toString and constructor as missing items", () => {
    const { container } = render(<Cart initialLines={brokenLinkCart()} />);
    const missing = [...container.querySelectorAll("li.missing .name")].map((span) => span.textContent);
    expect(missing.join(" | "), "both odd ids should be shown as no longer available").toBe(
      "toString is no longer available | constructor is no longer available",
    );
    const text = container.textContent ?? "";
    expect(text.includes("native code") || text.includes("function"), "the cart should never show a JavaScript function as an item").toBe(false);
    expect(text.includes("NaN"), "the cart should never show NaN").toBe(false);
  });

  it("works out the bill from the real items only", () => {
    const { container } = render(<Cart initialLines={brokenLinkCart()} />);
    expect(container.querySelector(".count")?.textContent, "only the tote bag can be bought").toBe("1 item");
    expect(container.querySelector(".subtotal")?.textContent, "the tote bag alone is ₹899.00").toBe("Subtotal: ₹899.00");
    expect(container.querySelector(".total")?.textContent, "₹899.00 is not above ₹999, so ₹60.00 delivery: ₹959.00").toBe("Total: ₹959.00");
  });

  it("still gives free delivery when the real items pass ₹999", () => {
    const lines = [
      { id: "DC-1045", qty: 1 },
      { id: "DC-1043", qty: 1 },
      { id: "DC-1031", qty: 3 },
    ];
    const { container } = render(<Cart initialLines={lines} />);
    expect(container.querySelector(".delivery")?.textContent, "₹899 + ₹150 = ₹1,049, above ₹999, so delivery is free").toBe("Delivery: Free");
    expect(container.querySelector(".total")?.textContent, "the total should be ₹1,049.00").toBe("Total: ₹1,049.00");
  });
});
