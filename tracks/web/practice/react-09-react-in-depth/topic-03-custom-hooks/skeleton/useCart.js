import { useEffect, useState } from "react";

/** The cart saved under key, or an empty cart if nothing readable is saved. */
export function loadLines(key) {
  try {
    const saved = localStorage.getItem(key);
    return saved ? JSON.parse(saved) : [];
  } catch {
    return [];
  }
}

/** The cart's lines and what you can do with them. Each line is { code, name, paise, qty }. */
export function useCart(storageKey = "dhaaga-cart") {
  // TODO 1: keep the lines in state, starting from loadLines(storageKey), called only on the first render.
  const lines = [];

  // TODO 2: save the lines to localStorage under storageKey, as JSON, whenever they change.

  function add(item) {
    // TODO 3: add the item with qty 1, or raise the qty of the line with the same code. Use the updater form.
  }

  function remove(code) {
    // TODO 4: drop the line with this code.
  }

  // TODO 5: count is all the qty values added up; total is paise times qty for every line, in paise.
  const count = 0;
  const total = 0;
  return { lines, add, remove, count, total };
}
