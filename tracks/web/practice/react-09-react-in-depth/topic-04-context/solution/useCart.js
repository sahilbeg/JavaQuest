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
  const [lines, setLines] = useState(() => loadLines(storageKey));

  useEffect(() => {
    localStorage.setItem(storageKey, JSON.stringify(lines));
  }, [storageKey, lines]);

  function add(item) {
    setLines((current) => {
      const found = current.some((line) => line.code === item.code);
      if (found) {
        return current.map((line) => (line.code === item.code ? { ...line, qty: line.qty + 1 } : line));
      }
      return [...current, { ...item, qty: 1 }];
    });
  }

  function remove(code) {
    setLines((current) => current.filter((line) => line.code !== code));
  }

  const count = lines.reduce((sum, line) => sum + line.qty, 0);
  const total = lines.reduce((sum, line) => sum + line.paise * line.qty, 0);
  return { lines, add, remove, count, total };
}
