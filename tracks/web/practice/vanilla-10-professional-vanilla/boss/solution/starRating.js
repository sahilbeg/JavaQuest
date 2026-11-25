// Round 2, DOM: "Build a star rating for Golu's review form. No framework."

const LABELS = ["1 star", "2 stars", "3 stars", "4 stars", "5 stars"];

/** Draws five stars into root as a radio group. onChange(n) is called whenever the rating changes. */
export function createStarRating(root, { label, value = 0, onChange = () => {} }) {
  let rating = value;
  root.setAttribute("role", "radiogroup");
  root.setAttribute("aria-label", label);

  const stars = LABELS.map((text, index) => {
    const star = document.createElement("span");
    star.className = "star";
    star.textContent = "★";
    star.dataset.value = String(index + 1);
    star.setAttribute("role", "radio");
    star.setAttribute("aria-label", text);
    return star;
  });
  root.replaceChildren(...stars);

  function render() {
    stars.forEach((star, index) => {
      const number = index + 1;
      star.classList.toggle("filled", number <= rating);
      star.setAttribute("aria-checked", String(number === rating));
      const tabStop = rating === 0 ? number === 1 : number === rating;
      star.tabIndex = tabStop ? 0 : -1;
    });
  }

  function choose(next) {
    if (next === rating) {
      return;
    }
    rating = next;
    render();
    onChange(next);
  }

  root.addEventListener("click", (event) => {
    const star = event.target.closest(".star");
    if (star !== null) {
      choose(Number(star.dataset.value));
    }
  });

  root.addEventListener("keydown", (event) => {
    const star = event.target.closest(".star");
    if (star === null) {
      return;
    }
    const current = Number(star.dataset.value);
    let next = null;
    if (event.key === "ArrowRight" || event.key === "ArrowDown") {
      next = current === 5 ? 1 : current + 1;
    } else if (event.key === "ArrowLeft" || event.key === "ArrowUp") {
      next = current === 1 ? 5 : current - 1;
    } else if (event.key === " ") {
      next = current;
    }
    if (next === null) {
      return;
    }
    event.preventDefault();
    choose(next);
    stars[next - 1].focus();
  });

  render();
  return { getValue: () => rating };
}
