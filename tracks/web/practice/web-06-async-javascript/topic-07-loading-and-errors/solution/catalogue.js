// Loads Dhaaga's catalogue from GET /api/catalogue and draws it, with a status line for every state.
// The server answers with a list such as [{ "id": "DC-1042", "name": "Golu the elephant", "paise": 45000 }].

export const LOADING = "Loading the catalogue...";
export const EMPTY = "No items yet. Check back soon!";
export const FAILED = "Couldn't load the catalogue. Check your connection and try again.";

/**
 * list is the <ul> for the items, status is the status line, and retry is the Try again button.
 * Sets status.dataset.state to "loading", then to "ready", "empty" or "error". Never throws.
 */
export async function loadCatalogue({ list, status, retry }) {
  retry.hidden = true;
  status.textContent = LOADING;
  status.dataset.state = "loading";

  try {
    const response = await fetch("/api/catalogue");
    if (!response.ok) {
      throw new Error(`Catalogue failed: ${response.status}`);
    }
    const items = await response.json();

    list.replaceChildren();
    for (const item of items) {
      const li = document.createElement("li");
      li.textContent = item.name;
      list.append(li);
    }

    if (items.length === 0) {
      status.textContent = EMPTY;
      status.dataset.state = "empty";
    } else {
      status.textContent = "";
      status.dataset.state = "ready";
    }
  } catch (error) {
    console.error(error);
    status.textContent = FAILED;
    status.dataset.state = "error";
    retry.hidden = false;
  }
}
