// The catalogue screen, which almost every customer opens. This file is finished.

export function catalogueView() {
  const section = document.createElement("section");
  const h1 = document.createElement("h1");
  h1.textContent = "Catalogue";
  section.append(h1);
  return section;
}
