import { describe, it, expect, afterEach, vi } from "vitest";
import { render, screen, cleanup } from "@testing-library/react";
import userEvent from "@testing-library/user-event";
import { CartButton, ProductTile, SearchBar } from "../src/ProductTile.jsx";

afterEach(cleanup);

describe("CartButton", () => {
  it("calls onAdd with the product's id when clicked", async () => {
    const user = userEvent.setup();
    const onAdd = vi.fn();
    render(<CartButton id="DC-1042" onAdd={onAdd} />);
    await user.click(screen.getByRole("button", { name: "Add to cart" }));
    expect(onAdd.mock.calls.length, "one click should call onAdd once").toBe(1);
    expect(onAdd.mock.calls[0][0], "onAdd should get the id DC-1042").toBe("DC-1042");
  });
});

describe("ProductTile", () => {
  it("opens the product when you click the tile", async () => {
    const user = userEvent.setup();
    const onOpen = vi.fn();
    render(<ProductTile id="DC-1042" name="Golu the elephant" paise={45000} onOpen={onOpen} onAdd={vi.fn()} />);
    await user.click(screen.getByText("Golu the elephant"));
    expect(onOpen.mock.calls.length, "clicking the tile's name should call onOpen once").toBe(1);
    expect(onOpen.mock.calls[0]?.[0], "onOpen should get the id DC-1042").toBe("DC-1042");
  });

  it("has an Add to cart button", () => {
    render(<ProductTile id="DC-1042" name="Golu the elephant" paise={45000} onOpen={vi.fn()} onAdd={vi.fn()} />);
    const button = screen.queryByRole("button", { name: "Add to cart" });
    expect(button, "the tile should show a CartButton").not.toBeNull();
  });

  it("adds to the cart without opening the product page", async () => {
    const user = userEvent.setup();
    const onOpen = vi.fn();
    const onAdd = vi.fn();
    render(<ProductTile id="DC-1043" name="Marigold coaster set" paise={15000} onOpen={onOpen} onAdd={onAdd} />);
    const button = screen.queryByRole("button", { name: "Add to cart" });
    expect(button, "the tile should show a CartButton").not.toBeNull();
    await user.click(button);
    expect(onAdd.mock.calls.length, "the button inside the tile should call onAdd once").toBe(1);
    expect(onAdd.mock.calls[0]?.[0], "the tile should pass its own id, DC-1043, to the button").toBe("DC-1043");
    expect(onOpen.mock.calls.length, "the click must stop at the button, so onOpen should not be called").toBe(0);
  });
});

describe("SearchBar", () => {
  it("searches for the typed text when you press Enter", async () => {
    const user = userEvent.setup();
    const onSearch = vi.fn();
    render(<SearchBar onSearch={onSearch} />);
    await user.type(screen.getByLabelText("Search the store"), "golu{Enter}");
    expect(onSearch.mock.calls.length, "pressing Enter should call onSearch once").toBe(1);
    expect(onSearch.mock.calls[0]?.[0], "onSearch should get the text in the box").toBe("golu");
  });

  it("does not search while you are still typing", async () => {
    const user = userEvent.setup();
    const onSearch = vi.fn();
    render(<SearchBar onSearch={onSearch} />);
    await user.type(screen.getByLabelText("Search the store"), "coaster");
    expect(onSearch.mock.calls.length, "typing letters without Enter should not call onSearch").toBe(0);
  });
});
