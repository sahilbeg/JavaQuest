// Starts the page for the preview. The tests never import this file.
import { createRoot } from "react-dom/client";
import { HoursPanel } from "./HoursPanel.tsx";
import { PageLayout } from "./PageLayout.tsx";
import { Panel } from "./Panel.tsx";

const root = document.getElementById("root");
if (root) {
  createRoot(root).render(
    <PageLayout header={<h1>Dhaaga & Co.</h1>}>
      <HoursPanel />
      <Panel title="Delivery">
        <p>Inside Pune: ₹60.00, free above ₹999.00</p>
      </Panel>
      <Panel title="Gift wrap">
        <p>₹30.00, with a handwritten card.</p>
      </Panel>
    </PageLayout>,
  );
}
