import { describe, it, expect } from "vitest";
import { visit, fakeServer, waitFor, textOf, reset } from "../src/testkit.js";

describe("visit", () => {
  it("changes the address and fires popstate once", () => {
    let fired = 0;
    const count = () => {
      fired += 1;
    };
    window.addEventListener("popstate", count);
    visit("/cart");
    window.removeEventListener("popstate", count);
    expect(location.pathname, "visit(\"/cart\") should change the address to /cart").toBe("/cart");
    expect(fired, "visit should fire exactly one popstate event, like pressing Back").toBe(1);
  });
});

describe("fakeServer", () => {
  const golu = { id: "DC-1042", name: "Golu the elephant", paise: 45000 };

  it("answers a known path with 200 and its JSON", async () => {
    const server = fakeServer({ "/api/items/DC-1042": golu });
    const response = await server.fetch("/api/items/DC-1042");
    expect(response.ok, "a known path should answer ok: true").toBe(true);
    expect(response.status, "a known path should answer status 200").toBe(200);
    expect(await response.json(), "json() should give the body from answers").toEqual(golu);
  });

  it("answers an unknown path with 404", async () => {
    const server = fakeServer({ "/api/items/DC-1042": golu });
    const response = await server.fetch("/api/items/DC-9999");
    expect(response.ok, "an unknown path should answer ok: false").toBe(false);
    expect(response.status, "an unknown path should answer status 404").toBe(404);
    expect(await response.json(), "the 404's json() should give { error: \"Not found\" }").toEqual({ error: "Not found" });
  });

  it("records every path it was asked for, in order", async () => {
    const server = fakeServer({ "/api/items": [] });
    await server.fetch("/api/items");
    await server.fetch("/api/items/DC-1042");
    expect(server.calls, "calls should list both paths, in the order they were asked").toEqual(["/api/items", "/api/items/DC-1042"]);
  });
});

describe("waitFor", () => {
  it("resolves at once with the value when the check already passes", async () => {
    expect(await waitFor(() => "ready"), "waitFor should resolve with what check() gave").toBe("ready");
  });

  it("keeps checking until the check passes", async () => {
    let value = null;
    setTimeout(() => {
      value = "Golu the elephant";
    }, 30);
    expect(await waitFor(() => value), "waitFor should keep trying until check() gives a value, then resolve with it").toBe("Golu the elephant");
  });

  it("gives up with a clear error after the timeout", async () => {
    let error = null;
    try {
      await waitFor(() => false, 50);
    } catch (e) {
      error = e;
    }
    expect(error?.message, "after 50 ms of failing checks, waitFor should reject with this message").toBe("waitFor timed out after 50 ms");
  });
});

describe("textOf", () => {
  it("reads text the way a person does", () => {
    const p = document.createElement("p");
    p.innerHTML = "\n  Golu   the\n   <b>elephant</b>  ";
    expect(textOf(p), "runs of spaces and new lines should become one space, with the ends trimmed").toBe("Golu the elephant");
  });
});

describe("reset", () => {
  it("clears the page, localStorage and the address", () => {
    document.body.innerHTML = "<p>Left over from the last test</p>";
    localStorage.setItem("dhaaga:cart", "[]");
    history.replaceState(null, "", "/cart");
    reset();
    expect(document.body.children.length, "reset should empty the body").toBe(0);
    expect(localStorage.getItem("dhaaga:cart"), "reset should clear localStorage").toBe(null);
    expect(location.pathname, "reset should put the address back to /").toBe("/");
  });
});
