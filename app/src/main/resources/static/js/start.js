import { h, icon } from "./dom.js";
import { api } from "./api.js";
import { refreshRewards } from "./rewards.js";
import { showCourse } from "./ladder.js";

// The course picker at /start: choose a goal, then a stack, see the whole path and the ladder, then start.
// A new learner lands here first; anyone can come back from "Switch course".

const STATUS = {
  ready: { label: "Ready", chip: "chip-done" },
  building: { label: "Being written", chip: "chip-progress" },
  soon: { label: "Coming soon", chip: "chip-todo" },
};

const hours = (minutes) => (minutes >= 60 ? `about ${Math.round(minutes / 60)} hours` : `${minutes} minutes`);

export async function renderStart(view, catalog, navigate) {
  const { objectives, courses, active, chosen } = await api.courses();
  const activeCourse = courses.find((c) => c.id === active);
  const state = {
    goal: (chosen && activeCourse?.objective) || null,
    course: (chosen && active) || null,
  };

  function draw() {
    const forGoal = courses.filter((c) => c.objective === state.goal);
    const course = courses.find((c) => c.id === state.course);
    // The ladder appears only once a course is chosen, and shows that course's ladder.
    showCourse(course ?? null);
    view.replaceChildren(h("div", { class: "start" },
      h("header", { class: "page-head" },
        h("p", { class: "eyebrow" }, chosen ? "Switch course" : "Welcome to Cadence"),
        h("h1", {}, "What do you want to become?"),
        h("p", { class: "muted" }, chosen
          ? "Your progress in every course is kept. Switching only changes which course you're working on."
          : "Pick a goal, then the tools you want to learn it with. You'll see your whole path before you start.")),

      h("section", { class: "start-step", "aria-labelledby": "goal-title" },
        h("h2", { id: "goal-title", class: "start-step-title" }, h("span", { class: "start-num" }, "1"), "Choose your goal"),
        h("div", { class: "start-grid", role: "radiogroup", "aria-labelledby": "goal-title" }, objectives.map((goal) => {
          const options = courses.filter((c) => c.objective === goal.id);
          const ready = options.filter((c) => c.status === "ready").length;
          return choiceCard({
            selected: state.goal === goal.id,
            title: goal.title,
            body: goal.summary,
            meta: `${options.length} ${options.length === 1 ? "stack" : "stacks"}${ready ? ` · ${ready} ready` : ""}`,
            onclick: () => { state.goal = goal.id; state.course = options.length === 1 ? options[0].id : null; draw(); focusStep("stack-title"); },
          });
        }))),

      state.goal && h("section", { class: "start-step", "aria-labelledby": "stack-title" },
        h("h2", { id: "stack-title", class: "start-step-title", tabindex: "-1" }, h("span", { class: "start-num" }, "2"), "Choose your stack"),
        h("div", { class: "start-grid start-grid-wide", role: "radiogroup", "aria-labelledby": "stack-title" }, forGoal.map((c) => choiceCard({
          selected: state.course === c.id,
          title: c.title,
          body: c.summary,
          chips: c.stack,
          status: c.status,
          meta: c.id === active && chosen ? "Your course now" : null,
          onclick: () => { state.course = c.id; draw(); focusStep("path-title"); },
        })))),

      course && pathPanel(course)));
  }

  function pathPanel(course) {
    const current = course.id === active && chosen;
    const started = course.mastered > 0;
    const action = current ? "Continue this course" : started ? "Switch to this course" : "Start this course";
    const note = !course.canStart && (course.status === "building"
      ? "This course is being written. It opens here as soon as every module is ready."
      : "This course is planned. Pick a ready one to start now; your progress will count when you switch later.");
    return h("section", { class: "start-step start-path", "aria-labelledby": "path-title" },
      h("h2", { id: "path-title", class: "start-step-title", tabindex: "-1" }, h("span", { class: "start-num" }, "3"), "Your path"),
      h("div", { class: "start-path-head" },
        h("div", {},
          h("p", { class: "start-path-title" }, course.title),
          h("p", { class: "muted" }, course.tickets
            ? `${course.path.length} modules · ${course.tickets} tickets · ${hours(course.minutes)} of lessons`
            : `${course.path.length} modules planned`,
            started ? ` · ${course.mastered} done` : "")),
        h("button", { type: "button", class: "button button-primary button-big", disabled: !course.canStart || null, onclick: start },
          action, icon("arrow-right"))),
      note && h("p", { class: "start-note" }, note),
      h("div", { class: "start-columns" },
        h("ol", { class: "start-modules" }, course.path.map((m, i) => h("li", { class: m.tickets ? "" : "is-planned" },
          h("span", { class: "start-module-num" }, String(i).padStart(2, "0")),
          h("span", { class: "start-module-title" }, m.title, m.project && h("span", { class: "muted" }, ` · ${m.project}`)),
          h("span", { class: "start-module-meta" }, m.tickets ? (m.mastered ? `${m.mastered}/${m.tickets}` : `${m.tickets} tickets`) : "Being written")))),
        h("div", { class: "start-ladder" },
          h("p", { class: "eyebrow" }, "The ladder you'll climb"),
          h("ol", {}, [...course.ladder].reverse().map((title, i, all) =>
            h("li", { class: i === all.length - 1 ? "is-first" : "" }, title))))));

    async function start(event) {
      const button = event.currentTarget;
      button.disabled = true;
      try {
        if (!current) await api.chooseCourse(course.id);
        await refreshRewards();
        navigate("/");
      } catch (error) {
        button.disabled = false;
        view.querySelector(".start-path")?.append(h("p", { class: "start-note", role: "alert" }, error.message));
      }
    }
  }

  draw();
  return () => {};
}

function choiceCard({ selected, title, body, chips = [], status, meta, onclick }) {
  return h("button", { type: "button", role: "radio", "aria-checked": String(selected), class: `start-card${selected ? " is-selected" : ""}`, onclick },
    h("span", { class: "start-card-top" },
      h("span", { class: "start-card-title" }, title),
      status && h("span", { class: `chip ${STATUS[status].chip}` }, STATUS[status].label)),
    body && h("span", { class: "start-card-body" }, body),
    chips.length > 0 && h("span", { class: "start-chips" }, chips.map((c) => h("span", { class: "start-chip" }, c))),
    meta && h("span", { class: "start-card-meta" }, meta));
}

function focusStep(id) {
  requestAnimationFrame(() => {
    const el = document.getElementById(id);
    el?.scrollIntoView({ behavior: "smooth", block: "start" });
    el?.focus({ preventScroll: true });
  });
}
