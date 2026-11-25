// A small version of the dhaaga.in store: a router with a catalogue and item pages that fetch from the API.
// This file is finished. The checks in test/app.test.js drive it with your test kit.

function el(tag, text) {
  const element = document.createElement(tag);
  element.textContent = text;
  return element;
}

function rupees(paise) {
  return `₹${(paise / 100).toFixed(2)}`;
}

function catalogueView(items) {
  const section = document.createElement("section");
  const list = document.createElement("ul");
  for (const item of items) {
    const link = el("a", item.name);
    link.href = `/items/${item.id}`;
    link.dataset.link = "";
    const li = document.createElement("li");
    li.append(link);
    list.append(li);
  }
  section.append(el("h1", "Catalogue"), list);
  return section;
}

function itemView(item) {
  const section = document.createElement("section");
  section.append(el("h1", item.name), el("p", rupees(item.paise)));
  return section;
}

/** Starts the app in root. fetch is passed in, so tests can hand it a pretend server. Returns stop(). */
export function startApp(root, { fetch }) {
  let latest = 0;

  async function load(url, view) {
    const ticket = ++latest;
    root.replaceChildren(el("p", "Loading..."));
    try {
      const response = await fetch(url);
      if (ticket !== latest) {
        return;
      }
      if (!response.ok) {
        root.replaceChildren(el("h1", "Item not found"));
        return;
      }
      const data = await response.json();
      if (ticket === latest) {
        root.replaceChildren(view(data));
      }
    } catch {
      if (ticket === latest) {
        root.replaceChildren(el("p", "Couldn't load this page. Please try again."));
      }
    }
  }

  function render() {
    const parts = location.pathname.split("/");
    if (location.pathname === "/") {
      latest++;
      root.replaceChildren(el("h1", "Dhaaga & Co."));
    } else if (location.pathname === "/items") {
      load("/api/items", catalogueView);
    } else if (parts.length === 3 && parts[1] === "items" && parts[2] !== "") {
      load(`/api/items/${parts[2]}`, itemView);
    } else {
      latest++;
      root.replaceChildren(el("h1", "Page not found"));
    }
  }

  function onClick(event) {
    const link = event.target.closest("a[data-link]");
    if (link === null || event.ctrlKey || event.metaKey) {
      return;
    }
    event.preventDefault();
    history.pushState(null, "", link.getAttribute("href"));
    render();
  }

  window.addEventListener("popstate", render);
  document.addEventListener("click", onClick);
  render();

  return () => {
    window.removeEventListener("popstate", render);
    document.removeEventListener("click", onClick);
  };
}
