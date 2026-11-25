// Round 2, DOM: "Build a star rating for Golu's review form. No framework."
// The candidate's answer. It worked for them with a mouse, and with nothing else.

const LABELS = ["1 star", "2 stars", "3 stars", "4 stars", "5 stars"];

/** Draws five stars into root as a radio group. onChange(n) is called whenever the rating changes. */
export function createStarRating(root, { label, value = 0, onChange = () => {} }) {
  let rating = value;
  // TODO 2: make root a radio group: role="radiogroup" and aria-label set to label.
  //         Give each star role="radio" and its aria-label from LABELS ("1 star", "2 stars", ...).

  const stars = LABELS.map((text, index) => {
    const star = document.createElement("span");
    star.className = "star";
    star.textContent = "★";
    star.dataset.value = String(index + 1);
    return star;
  });
  root.replaceChildren(...stars);

  function render() {
    stars.forEach((star, index) => {
      const number = index + 1;
      star.classList.toggle("filled", number <= rating);
      // TODO 3: aria-checked "true" only on the chosen star ("false" on the others), and a roving tabindex:
      //         tabIndex 0 on the chosen star (on the first star when nothing is chosen), -1 on every other star
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

  // TODO 4: one keydown listener on root, for the star that has focus:
  //         ArrowRight and ArrowDown choose the next star (5 wraps to 1), ArrowLeft and ArrowUp the one before
  //         (1 wraps to 5), and Space (" ") chooses the focused star. For those keys only: preventDefault,
  //         choose(next), and move focus to the chosen star.

  render();
  return { getValue: () => rating };
}
