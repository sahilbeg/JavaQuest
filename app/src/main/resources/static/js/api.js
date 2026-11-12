const WRITE_HEADERS = { "X-JavaQuest": "1", "Content-Type": "application/json" };

async function parse(response) {
  if (response.ok) {
    const text = await response.text();
    return text ? JSON.parse(text) : null;
  }
  let message = `The server answered ${response.status}.`;
  try {
    const body = await response.json();
    message = body.message ?? body.detail ?? message;
  } catch {
    // The body was not JSON; keep the status message.
  }
  throw new Error(message);
}

export const topicPath = (id) => `/api/topics/${id}`;

export const api = {
  catalog: () => fetch("/api/catalog").then(parse),
  topic: (id) => fetch(topicPath(id)).then(parse),
  hints: (id) => fetch(`${topicPath(id)}/hints`).then(parse),
  nextHint: (id) => fetch(`${topicPath(id)}/hints/next`, { method: "POST", headers: WRITE_HEADERS }).then(parse),
  progress: (id) => fetch(`${topicPath(id)}/progress`).then(parse),
  solution: (id) => fetch(`${topicPath(id)}/solution`, { method: "POST", headers: WRITE_HEADERS }).then(parse),
  reset: (id) => fetch(`${topicPath(id)}/reset`, { method: "POST", headers: WRITE_HEADERS }).then(parse),
  quiz: (id) => fetch(`${topicPath(id)}/quiz`).then(parse),
  gradeQuiz: (id, seed, answers) =>
    fetch(`${topicPath(id)}/quiz`, { method: "POST", headers: WRITE_HEADERS, body: JSON.stringify({ seed, answers }) }).then(parse),
  readSource: (id, path) => fetch(`${topicPath(id)}/source?path=${encodeURIComponent(path)}`).then(parse),
  writeSource: (id, path, content) =>
    fetch(`${topicPath(id)}/source`, { method: "PUT", headers: WRITE_HEADERS, body: JSON.stringify({ path, content }) }).then(parse),
  submit: (id) => fetch(`${topicPath(id)}/submit`, { method: "POST", headers: WRITE_HEADERS }).then(parse),
  runMain: (id) => fetch(`${topicPath(id)}/run`, { method: "POST", headers: WRITE_HEADERS }).then(parse),
  runSnippet: (code) =>
    fetch("/api/snippets/run", { method: "POST", headers: WRITE_HEADERS, body: JSON.stringify({ code }) }).then(parse),
};
