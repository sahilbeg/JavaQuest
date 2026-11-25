import { describe, it, expect, afterEach } from "vitest";
import { render, cleanup } from "@testing-library/react";
import { StoreHeader } from "../src/StoreHeader.tsx";

afterEach(cleanup);

describe("StoreHeader", () => {
  it("is a header with the store-header class", () => {
    const { container } = render(<StoreHeader />);
    const header = container.querySelector("header.store-header");
    expect(header, "StoreHeader should return a <header className=\"store-header\">").not.toBeNull();
  });

  it("names the store in the main heading", () => {
    const { container } = render(<StoreHeader />);
    expect(container.querySelector("header h1")?.textContent, "the <h1> inside the header should say Dhaaga & Co.").toBe("Dhaaga & Co.");
  });

  it("shows the tagline", () => {
    const { container } = render(<StoreHeader />);
    expect(container.querySelector("header p.tagline")?.textContent, "the tagline should say Handmade crochet from Pune").toBe(
      "Handmade crochet from Pune",
    );
  });
});
