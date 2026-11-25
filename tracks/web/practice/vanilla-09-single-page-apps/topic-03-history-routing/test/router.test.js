import { describe, it, expect, beforeEach, afterEach, vi } from "vitest";
import { createRouter } from "../src/router.js";

function heading(text) {
  const h1 = document.createElement("h1");
  h1.textContent = text;
  return h1;
}

const routes = [
  { path: "/", view: () => heading("Home") },
  { path: "/items", view: () => heading("Catalogue") },
  { path: "/items/:id", view: ({ id }) => heading(`Item ${id}`) },
  { path: "/cart", view: () => heading("Cart") },
];
const notFound = (path) => heading(`Not found: ${path}`);

let outlet;
let router;

beforeEach(() => {
  history.replaceState(null, "", "/");
  document.body.innerHTML = `
    <nav>
      <a id="home" href="/" data-link>Home</a>
      <a id="golu" href="/items/DC-1042" data-link><span>Golu</span></a>
      <a id="cart" href="/cart" data-link>Cart</a>
      <a id="whatsapp" href="https://wa.me/919876543210">WhatsApp us</a>
    </nav>
    <main id="app"></main>
  `;
  outlet = document.querySelector("#app");
  router = createRouter(routes, outlet, notFound);
});

afterEach(() => {
  router.stop();
  vi.restoreAllMocks();
});

const screen = () => outlet.textContent;

/**
 * Clicks an element like a finger would. Returns true if the router called preventDefault.
 * (A listener on window runs last; it notes the answer, then stops jsdom from trying to load a real page.)
 */
function click(element, keys = {}) {
  let prevented = null;
  const note = (event) => {
    prevented = event.defaultPrevented;
    event.preventDefault();
  };
  window.addEventListener("click", note);
  element.dispatchEvent(new MouseEvent("click", { bubbles: true, cancelable: true, ...keys }));
  window.removeEventListener("click", note);
  return prevented;
}

/** What the browser does on Back: the address changes, then popstate fires. */
function pressBackTo(path) {
  history.replaceState(null, "", path);
  window.dispatchEvent(new PopStateEvent("popstate"));
}

describe("start", () => {
  it("draws the screen for the address the page opened with", () => {
    history.replaceState(null, "", "/items/DC-1042");
    router.start();
    expect(screen(), "opening dhaaga.in/items/DC-1042 should show Golu's screen straight away").toBe("Item DC-1042");
  });

  it("draws the home screen at /", () => {
    router.start();
    expect(screen(), "at / the home screen should show").toBe("Home");
  });

  it("shows the not found screen for an unknown address", () => {
    history.replaceState(null, "", "/hats");
    router.start();
    expect(screen(), "an address no route knows should show notFound(path)").toBe("Not found: /hats");
  });
});

describe("navigate", () => {
  it("changes the address and draws the new screen", () => {
    router.start();
    router.navigate("/cart");
    expect(location.pathname, "navigate(\"/cart\") should change the address to /cart").toBe("/cart");
    expect(screen(), "navigate(\"/cart\") should draw the cart").toBe("Cart");
  });

  it("adds a history entry with pushState, so Back can return", () => {
    const push = vi.spyOn(history, "pushState");
    router.start();
    router.navigate("/items");
    expect(push.mock.calls.length, "navigate should call history.pushState once").toBe(1);
    expect(push.mock.calls[0]?.[2], "pushState's third argument should be the new path").toBe("/items");
  });

  it("swaps the screen instead of adding another", () => {
    router.start();
    router.navigate("/cart");
    router.navigate("/items");
    expect(outlet.children.length, "the outlet should hold exactly one screen; use replaceChildren").toBe(1);
  });
});

describe("link clicks", () => {
  it("handles a tap on an app link without loading a new page", () => {
    router.start();
    const prevented = click(document.querySelector("#golu span"));
    expect(prevented, "a tap on a data-link link should call event.preventDefault()").toBe(true);
    expect(location.pathname, "the address should become /items/DC-1042").toBe("/items/DC-1042");
    expect(screen(), "Golu's screen should show").toBe("Item DC-1042");
  });

  it("leaves links without data-link alone", () => {
    router.start();
    const prevented = click(document.querySelector("#whatsapp"));
    expect(prevented, "the WhatsApp link isn't an app link, so the router must not stop it").toBe(false);
    expect(location.pathname, "the address should stay at /").toBe("/");
  });

  it("leaves Ctrl-click and Cmd-click to the browser, for opening a new tab", () => {
    router.start();
    expect(click(document.querySelector("#cart"), { ctrlKey: true }), "Ctrl-click must not be stopped").toBe(false);
    expect(click(document.querySelector("#cart"), { metaKey: true }), "Cmd-click must not be stopped").toBe(false);
    expect(location.pathname, "the address should stay at /").toBe("/");
  });
});

describe("Back and Forward", () => {
  it("redraws when popstate fires", () => {
    router.start();
    router.navigate("/cart");
    pressBackTo("/");
    expect(screen(), "after Back to /, the home screen should show again").toBe("Home");
  });

  it("doesn't add a history entry when Back is pressed", () => {
    router.start();
    router.navigate("/cart");
    const push = vi.spyOn(history, "pushState");
    pressBackTo("/");
    expect(push.mock.calls.length, "popstate should only redraw; calling pushState here would break Forward").toBe(0);
  });
});

describe("stop", () => {
  it("removes both listeners", () => {
    router.start();
    router.stop();
    expect(click(document.querySelector("#cart")), "after stop(), link taps must not be handled").toBe(false);
    pressBackTo("/items");
    expect(screen(), "after stop(), popstate must not redraw").toBe("Home");
  });
});
