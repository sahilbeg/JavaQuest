// Kakashi's walkthrough for writing a ticket's code in VS Code: set it up once, open the practice folder,
// find this ticket's files, write at each TODO, and save to submit. Every path, file name and TODO number
// comes from the ticket itself (TopicDetail.codeGuide), so the steps fit every code ticket.

import { h, icon, avatar, toast, TEAM } from "./dom.js";

const MAC = navigator.platform.startsWith("Mac");
const OPEN_KEY = "jq.vscode.guideOpen";
const SETUP_KEY = "jq.vscode.setupDone";

function stored(key) {
  try { return localStorage.getItem(key); } catch { return null; }
}

function store(key, value) {
  try { localStorage.setItem(key, value); } catch { /* the guide works without it */ }
}

/** A keyboard shortcut for this computer: the Mac keys on a Mac, the Windows and Linux keys elsewhere. */
function keys(mac, other) {
  return h("kbd", {}, MAC ? mac : other);
}

/** A vscode:// link opens VS Code at a folder, or at a file and line. */
export function vscodeUrl(path, line) {
  let p = path.replace(/\\/g, "/");
  if (!p.startsWith("/")) p = `/${p}`;
  return `vscode://file${encodeURI(p)}${line ? `:${line}` : ""}`;
}

function copyButton(text, what) {
  return h("button", {
    type: "button", class: "button button-quiet button-sm",
    onclick: () => navigator.clipboard.writeText(text)
      .then(() => toast(`Copied the ${what}`))
      .catch(() => toast("Couldn't copy. Select the text and copy it yourself.")),
  }, icon("copy"), "Copy");
}

function openLink(url, label) {
  return h("a", { class: "button button-sm", href: url }, icon("external-link"), label);
}

function step(n, title, ...body) {
  return h("section", { class: "vg-step", "aria-labelledby": `vg-step-${n}` },
    h("h3", { id: `vg-step-${n}` }, h("span", { class: "vg-num" }, String(n)), title),
    ...body);
}

const ol = (...items) => h("ol", { class: "vg-list" }, items.filter(Boolean).map((i) => h("li", {}, i)));

function note(kind, ...text) {
  return h("p", { class: `vg-note vg-note-${kind}` }, icon(kind === "warning" ? "triangle-alert" : "lightbulb"), h("span", {}, ...text));
}

// Web tickets (JavaScript, React) need no extension and have package.json where Java has pom.xml.
let web = false;

function setupStep() {
  const done = stored(SETUP_KEY) === "1";
  const box = h("details", { class: "vg-setup", open: !done });
  const check = h("input", { type: "checkbox", checked: done, onchange: () => {
    store(SETUP_KEY, check.checked ? "1" : "0");
    if (check.checked) box.open = false;
  } });
  box.append(
    h("summary", {}, h("span", {}, "Four steps, needed only once. Tick the box at the end and they fold away."), icon("chevron-down")),
    ol(
      ["Download VS Code from ", h("a", { href: "https://code.visualstudio.com", target: "_blank", rel: "noopener" }, "code.visualstudio.com"),
        ", install it, and open it."],
      ["Open the Extensions panel: press ", keys("Cmd Shift X", "Ctrl Shift X"),
        ". It's also the icon with four small squares on the bar at the far left."],
      web
        ? ["In its search box, type ", h("strong", {}, "ESLint"), ". Pick the one made by Microsoft, then click ",
            h("strong", {}, "Install"), ". It underlines JavaScript mistakes as you type."]
        : ["In its search box, type ", h("strong", {}, "Extension Pack for Java"),
            ". Pick the one made by Microsoft, then click ", h("strong", {}, "Install"), "."],
      web ? "VS Code already understands HTML, CSS, JavaScript and React. Nothing else to install."
        : "Wait until the install finishes. It uses the Java (the JDK) you already have, the same one that runs Cadence.",
    ),
    h("label", { class: "vg-check" }, check, "I've set up VS Code"));
  return step(1, "Set up VS Code (first time only)", box);
}

function openFolderStep(guide) {
  return step(2, "Open the practice folder",
    h("p", { class: "vg-lead" }, "Already open from an earlier ticket? Skip this step. You open it once and leave it open."),
    ol(
      ["Click this button: ", openLink(vscodeUrl(guide.practiceRoot), "Open the practice folder in VS Code"),
        " Your browser asks if it may open Visual Studio Code. Click ", h("strong", {}, "Open"), "."],
      ["No luck with the button? Do it by hand. In VS Code, open the ", h("strong", {}, "File"), " menu and choose ",
        h("strong", {}, "Open Folder…"), "."],
      ["Go to this folder: ", h("code", { class: "vg-path" }, guide.practiceRoot), " ", copyButton(guide.practiceRoot, "folder path"),
        MAC
          ? [" In the window that opens, press ", keys("Cmd Shift G", ""), ", paste the path, press Return, then click ", h("strong", {}, "Open"), "."]
          : [" Paste the path into the address bar at the top of the window, press Enter, then click ", h("strong", {}, "Select Folder"), "."]],
      ["If VS Code asks “Do you trust the authors of the files in this folder?”, click ",
        h("strong", {}, "Yes, I trust the authors"), ". It's your own folder."],
      web ? "VS Code reads package.json to find React and the test tools. Cadence installed them when it started."
        : "The first time, give it about a minute. VS Code reads pom.xml to find JUnit and Spring. A small spinner turns in the bottom bar while it works.",
    ),
    note("tip", "Why the whole practice folder? Its ", h("code", {}, web ? "package.json" : "pom.xml"),
      " tells VS Code where the libraries are. Open one file on its own and every import turns red."));
}

function readTaskStep(guide, folders) {
  return step(3, "Read the task",
    ol(
      ["Show the Explorer, the list of files on the left: press ", keys("Cmd Shift E", "Ctrl Shift E"), "."],
      ["Click ", h("strong", {}, folders[0]), ", then ", h("strong", {}, folders[1]), ". Each click opens a folder."],
      guide.readme && ["Click ", h("strong", {}, "README.md"), ". It's the full task, step by step. Press ",
        keys("Cmd Shift V", "Ctrl Shift V"), " to read it nicely formatted. ", openLink(vscodeUrl(guide.readme), "Open README.md")],
    ));
}

function filesStep(guide) {
  const toChange = guide.files.filter((f) => f.toChange);
  const others = guide.files.filter((f) => !f.toChange);
  const name = (f) => f.path.split("/").pop();
  const pkg = guide.files[0].path.split("/").slice(0, -1).join("/");
  const fileRow = (f) => h("li", { class: "vg-file" },
    icon("file-code"), h("code", {}, name(f)),
    f.todos.length > 0 && h("span", { class: "chip chip-todo" }, `TODO ${f.todos.join(", ")}`),
    h("span", { class: "spacer" }),
    openLink(vscodeUrl(f.absolutePath, f.line), "Open in VS Code"));
  return step(4, toChange.length === 1 ? "Open the file you will change" : `Open the ${toChange.length} files you will change`,
    ol(
      ["In the Explorer, click ", h("strong", {}, "src"), pkg && [", then ", h("strong", {}, pkg),
        ". VS Code shows these nested folders as one row. That's normal"], "."],
      [toChange.length === 1 ? "This is the file to change. Click it, or use the button:" : "These are the files to change. Do them in this order:",
        h("ul", { class: "vg-files" }, toChange.map(fileRow))],
      ["Quicker way to open any file: press ", keys("Cmd P", "Ctrl P"), ", type its name, such as ",
        h("code", {}, name(toChange[0] ?? guide.files[0])), ", and press Return."],
      others.length > 0 && ["These files belong to the ticket too, but need no change. Read them to see what you're working with: ",
        others.map((f, i) => [i ? ", " : "", h("code", {}, name(f))]), "."],
    ),
    note("warning", "Write only inside ", h("strong", {}, "src"), ". Leave ", h("strong", {}, "skeleton"), ", ",
      h("strong", {}, "solution"), " and ", h("strong", {}, "test"), " alone. ", h("strong", {}, "solution"),
      ` holds ${TEAM.maya.name}'s answer and `, h("strong", {}, "test"), " holds the checks, so peeking spoils the practice."));
}

function writeStep(topic) {
  return step(5, "Write the code",
    ol(
      ["Press ", keys("Cmd F", "Ctrl F"), " and type ", h("code", {}, "TODO"),
        ". VS Code jumps to the first task. Press Return to jump to the next one."],
      "Read the TODO comment. It says what to write. Write your code on the lines just under it.",
      `A red wavy line means ${web ? "JavaScript" : "Java"} can't understand that line yet. Rest the mouse on it to read why.`,
      "Every import you need is already at the top of the file. You don't have to add any.",
      "Finished a TODO? Delete the comment or keep it. The checks don't read comments.",
      topic.mainClass && web && ["Want to see what your program prints? Press ", h("strong", {}, "Run"),
        " in Cadence's editor, or type ", h("code", {}, `node src/${topic.mainClass}`), " in VS Code's terminal."],
      web && topic.preview && "Want to see your page? Cadence's editor shows a live preview under the code. It updates each time you save.",
      topic.mainClass && !web && ["Want to see what your program prints? Click ", h("strong", {}, "Run"),
        ", the small word just above ", h("code", {}, "public static void main"),
        ". The output shows in the Terminal panel at the bottom. If the program waits for typing, click in the Terminal and type there."],
    ));
}

function submitStep() {
  return step(6, "Submit it for review",
    ol(
      ["Save: press ", keys("Cmd S", "Ctrl S"), ". Changed more than one file? Save each one, or press ",
        keys("Cmd Option S", "Ctrl K, then S"), " to save them all."],
      [h("strong", {}, "That's it: saving is how you submit."), " Cadence notices the save and runs the checks within a second or two."],
      ["Come back to this page. The ", h("strong", {}, "Checks"), " list below shows each check, green or red, marked “from your IDE save”."],
      "A red check says what it expected. Fix that part in VS Code, save again, and look again. Repeat until every check is green.",
      ["Stuck? Press ", h("strong", {}, "Hints"), " on this page. ", h("strong", {}, "Show solution"), " is below the editor, as a last resort."],
    ),
    note("tip", "If ", h("strong", {}, "File → Auto Save"), " is ticked, VS Code saves after every change, so the checks run as you type. That's fine. Untick it if you'd rather submit only when you press ",
      keys("Cmd S", "Ctrl S"), "."));
}

/** The walkthrough card above the code editor, or null for a ticket without code. */
export function vscodeGuide(topic) {
  web = topic.language === "javascript";
  const guide = topic.codeGuide;
  if (!guide || !guide.files.length) return null;
  const folders = topic.summary.id.split("/");
  const kakashi = TEAM.theo;
  const open = stored(OPEN_KEY) !== "0";
  const box = h("details", { class: "vscode-guide", open });
  box.addEventListener("toggle", () => store(OPEN_KEY, box.open ? "1" : "0"));
  box.append(
    h("summary", { class: "vg-head" },
      avatar("theo", "avatar-lg"),
      h("span", { class: "vg-title" }, h("strong", {}, "Code it in VS Code"),
        h("span", {}, `${kakashi.name} walks you through it, step by step`)),
      icon("chevron-down")),
    h("div", { class: "vg-body" },
      h("div", { class: "ticket-message vg-intro" },
        avatar("theo", "avatar-lg"),
        h("div", { class: "bubble" },
          h("p", { class: "who" }, h("strong", {}, kakashi.name), ` · ${kakashi.role}`),
          h("p", {}, "Yo. You'll write this ticket's code in VS Code, and Cadence checks it the moment you save."),
          h("p", {}, "Follow the steps in order. Each one is small. I'll be right here."))),
      h("div", { class: "vg-steps" },
        setupStep(), openFolderStep(guide), readTaskStep(guide, folders), filesStep(guide), writeStep(topic), submitStep()),
      h("p", { class: "vg-outro" }, avatar("theo"),
        "Every check green? Then the ticket's done. Not bad. Now, back to my book.")));
  return box;
}
