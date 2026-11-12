const topicsEl = document.getElementById("topics");

function el(tag, attrs = {}, ...children) {
  const node = document.createElement(tag);
  Object.assign(node, attrs);
  node.append(...children);
  return node;
}

function renderResult(practice) {
  if (!practice) return el("p", {}, "Not run yet.");
  const r = practice.result;
  const box = el("div");
  const passed = r.outcome === "PASSED";
  box.append(el("p", { className: passed ? "passed" : "failed" },
    `${r.outcome} (from ${practice.source.toLowerCase()})`));
  for (const t of r.tests) {
    box.append(el("p", {}, `${t.passed ? "✔" : "✘"} ${t.name}${t.message ? ": " + t.message : ""}`));
  }
  for (const e of r.compileErrors) {
    box.append(el("p", { className: "failed" }, `${e.file} line ${e.line}: ${e.message}`));
  }
  if (r.output) box.append(el("pre", {}, r.output));
  return box;
}

function renderTopic(topic) {
  const result = el("div", { id: `result-${topic.id}` }, renderResult(topic.latest));
  const card = el("section", { className: "topic" }, el("h2", {}, topic.title), el("p", {}, topic.id));
  if (topic.practiceKind === "CODE") {
    const button = el("button", { type: "button" }, "Submit");
    button.addEventListener("click", async () => {
      button.disabled = true;
      try {
        await fetch(`/api/topics/${topic.id}/submit`, { method: "POST", headers: { "X-JavaQuest": "1" } });
      } finally {
        button.disabled = false;
      }
    });
    card.append(button);
  }
  card.append(result);
  return card;
}

const catalog = await (await fetch("/api/catalog")).json();
topicsEl.replaceChildren(...catalog.topics.map(renderTopic));

const events = new EventSource("/api/events");
events.addEventListener("run-started", (e) => {
  const { topicId } = JSON.parse(e.data);
  document.getElementById(`result-${topicId}`)?.replaceChildren(el("p", {}, "Running tests…"));
});
events.addEventListener("run-finished", (e) => {
  const practice = JSON.parse(e.data);
  document.getElementById(`result-${practice.topicId}`)?.replaceChildren(renderResult(practice));
});
