import { describe, it, expect, afterEach, vi } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { ProductList } from "../src/Catalogue.jsx";

afterEach(cleanup);

// This check has a file of its own: React warns about a missing key only once per component,
// so a list drawn by an earlier test could hide the warning.
describe("ProductList keys", () => {
  it("gives every row a key, so React logs no key warning", () => {
    const errors = vi.spyOn(console, "error").mockImplementation(() => {});
    try {
      const { container } = render(
        <ProductList
          products={[
            { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
            { id: "DC-1043", name: "Marigold coaster set", paise: 15000 },
          ]}
        />,
      );
      expect(container.querySelectorAll("li").length, "two products should make two <li> rows").toBe(2);
      const keyWarnings = errors.mock.calls.filter((args) => args.map(String).join(" ").includes("key"));
      expect(keyWarnings.length, "React warned about keys: put key={p.id} on the <li> that map returns").toBe(0);
    } finally {
      errors.mockRestore();
    }
  });
});
