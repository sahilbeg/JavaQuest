// The store's screens. Each view returns one element for the outlet. This file is finished.

const ITEMS = [
  { id: "DC-1042", name: "Golu the elephant", paise: 45000 },
  { id: "DC-2001", name: "Marigold coaster set", paise: 15000 },
  { id: "DC-3001", name: "Indigo tote bag", paise: 89900 },
];

function rupees(paise) {
  return `₹${(paise / 100).toFixed(2)}`;
}

function element(tag, text) {
  const el = document.createElement(tag);
  el.textContent = text;
  return el;
}

function link(href, text) {
  const a = element("a", text);
  a.href = href;
  return a;
}

export function homeView() {
  const section = document.createElement("section");
  section.append(element("h1", "Dhaaga & Co."), element("p", "Crochet from Pune, made by hand."), link("#/items", "See the catalogue"));
  return section;
}

export function catalogueView() {
  const section = document.createElement("section");
  const list = document.createElement("ul");
  for (const item of ITEMS) {
    const li = document.createElement("li");
    li.append(link(`#/items/${item.id}`, `${item.name}, ${rupees(item.paise)}`));
    list.append(li);
  }
  section.append(element("h1", "Catalogue"), list);
  return section;
}

export function itemView({ id }) {
  const item = ITEMS.find((it) => it.id === id);
  if (item === undefined) {
    return notFoundView(`/items/${id}`);
  }
  const section = document.createElement("section");
  section.append(element("h1", item.name), element("p", rupees(item.paise)), link("#/items", "Back to the catalogue"));
  return section;
}

export function cartView() {
  const section = document.createElement("section");
  section.append(element("h1", "Your cart"), element("p", "Your cart is empty."));
  return section;
}

export function notFoundView(path) {
  const section = document.createElement("section");
  section.append(element("h1", "Page not found"), element("p", `Nothing lives at ${path}.`), link("#/", "Go to the home page"));
  return section;
}

export const routes = [
  { path: "/", view: homeView },
  { path: "/items", view: catalogueView },
  { path: "/items/:id", view: itemView },
  { path: "/cart", view: cartView },
];
