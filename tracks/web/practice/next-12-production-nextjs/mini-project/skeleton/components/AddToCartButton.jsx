"use client";

import { useRouter } from "next/navigation";
import { CART_COOKIE, addToCartValue } from "../lib/cart.js";

// Adds one of an item to the cart cookie, then opens the cart. Already done: you don't need to change it.
export function AddToCartButton({ id, stock }) {
  const router = useRouter();
  if (stock === 0) {
    return (
      <button type="button" disabled>
        Sold out
      </button>
    );
  }
  function add() {
    const current = document.cookie
      .split("; ")
      .find((part) => part.startsWith(CART_COOKIE + "="))
      ?.slice(CART_COOKIE.length + 1);
    document.cookie = `${CART_COOKIE}=${addToCartValue(current ?? "", id)}; path=/; max-age=604800; samesite=lax`;
    router.push("/cart");
  }
  return (
    <button type="button" onClick={add}>
      Add to cart
    </button>
  );
}
