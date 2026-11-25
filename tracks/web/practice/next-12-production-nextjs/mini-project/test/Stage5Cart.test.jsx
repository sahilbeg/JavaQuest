import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import { readCart } from "../src/lib/cart.js";
import { CartView } from "../src/components/CartView.jsx";
import { resetStore } from "../src/lib/store.js";

vi.mock("next/navigation", () => ({
  notFound: vi.fn(() => {
    throw new Error("NEXT_NOT_FOUND");
  }),
  redirect: vi.fn((to) => {
    throw new Error("NEXT_REDIRECT " + to);
  }),
  useRouter: () => ({ push: vi.fn() }),
  usePathname: () => "/cart",
}));

/** A pretend cookie store, like the one cookies() gives: get("cart") returns { name, value } or undefined. */
function cookieStore(value) {
  return {
    get(name) {
      return name === "cart" && value !== undefined ? { name, value } : undefined;
    },
  };
}

const ONE_GOLU = [{ id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 1 }];
const BIG_CART = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 2 },
  { id: "DC-1301", name: "Tote bag", paise: 89900, qty: 1 },
];

beforeEach(resetStore);
afterEach(cleanup);

describe("Stage 5: readCart", () => {
  it("turns the cart cookie into lines with names and prices from the store", async () => {
    expect(await readCart(cookieStore("DC-1042:2,DC-1101:1")), "each id:qty should become { id, name, paise, qty } (TODO 8)").toEqual([
      { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 2 },
      { id: "DC-1101", name: "Marigold coaster set", paise: 15000, qty: 1 },
    ]);
  });

  it("gives an empty cart when there is no cookie", async () => {
    expect(await readCart(cookieStore(undefined)), "no cart cookie means no lines").toEqual([]);
  });

  it("skips entries it can't trust", async () => {
    expect(await readCart(cookieStore("DC-1042:2,DC-9999:1,DC-1101:0,DC-1402:abc,DC-1301:1")), "unknown items and bad quantities should be skipped (TODO 8)").toEqual([
      { id: "DC-1042", name: "Golu the elephant", paise: 45000, qty: 2 },
      { id: "DC-1301", name: "Tote bag", paise: 89900, qty: 1 },
    ]);
  });
});

describe("Stage 5: CartView", () => {
  it("lists each line and the totals, with ₹60 delivery for one Golu", () => {
    render(<CartView lines={ONE_GOLU} />);
    expect(screen.queryByText("Golu the elephant × 1: ₹450.00"), "the line should read Golu the elephant × 1: ₹450.00 (TODO 9)").not.toBeNull();
    expect(screen.queryByText("Subtotal: ₹450.00"), "Subtotal: ₹450.00 (TODO 9)").not.toBeNull();
    expect(screen.queryByText("Delivery: ₹60.00"), "one Golu pays delivery: Delivery: ₹60.00 (TODO 9)").not.toBeNull();
    expect(screen.queryByText("Total: ₹510.00"), "Total: ₹510.00 (TODO 9)").not.toBeNull();
    expect(screen.queryByText(/Diwali offer/), "there is no offer below ₹1,500, so no offer line").toBeNull();
  });

  it("shows the Diwali offer and free delivery on a big cart", () => {
    render(<CartView lines={BIG_CART} />);
    expect(screen.queryByText("Golu the elephant × 2: ₹900.00"), "two Golus cost ₹900.00 (TODO 9)").not.toBeNull();
    expect(screen.queryByText("Subtotal: ₹1,799.00"), "Subtotal: ₹1,799.00 (TODO 9)").not.toBeNull();
    expect(screen.queryByText("Diwali offer: ₹179.90 off"), "10% off ₹1,799.00 is ₹179.90 (TODO 9)").not.toBeNull();
    expect(screen.queryByText("Delivery: Free"), "above ₹999 after the offer, delivery is free (TODO 9)").not.toBeNull();
    expect(screen.queryByText("Total: ₹1,619.10"), "Total: ₹1,619.10 (TODO 9)").not.toBeNull();
  });

  it("has a checkout form with the lines, labelled fields and a Place order button", () => {
    const { container } = render(<CartView lines={BIG_CART} />);
    const hidden = [...container.querySelectorAll('input[type="hidden"][name="line"]')].map((input) => input.value);
    expect(hidden, "one hidden line field per cart line, like DC-1042:2 (TODO 9)").toEqual(["DC-1042:2", "DC-1301:1"]);
    expect(screen.queryByLabelText("Your name")?.getAttribute("name"), "a field labelled Your name, named name (TODO 9)").toBe("name");
    expect(screen.queryByLabelText("Phone number")?.getAttribute("name"), "a field labelled Phone number, named phone (TODO 9)").toBe("phone");
    expect(screen.queryByRole("button", { name: "Place order" }), "a Place order button (TODO 9)").not.toBeNull();
  });

  it("shows an empty cart without a form", () => {
    render(<CartView lines={[]} />);
    expect(screen.queryByText("Your cart is empty."), "an empty cart should say so (TODO 9)").not.toBeNull();
    expect(screen.queryByRole("link", { name: "Back to the shop" })?.getAttribute("href"), "a link back to / (TODO 9)").toBe("/");
    expect(screen.queryByRole("button", { name: "Place order" }), "no Place order button for an empty cart").toBeNull();
  });
});
