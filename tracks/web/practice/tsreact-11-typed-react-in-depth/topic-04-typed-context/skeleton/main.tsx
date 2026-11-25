import { createRoot } from "react-dom/client";
import { CustomerProvider } from "./CustomerContext.tsx";
import { AccountButton } from "./AccountButton.tsx";
import { Greeting } from "./Greeting.tsx";

function Header() {
  return (
    <header>
      <strong>Dhaaga &amp; Co.</strong> <AccountButton />
    </header>
  );
}

function Main() {
  return (
    <main>
      <h1>Handmade in Pune</h1>
      <section>
        <Greeting />
      </section>
    </main>
  );
}

createRoot(document.getElementById("root")!).render(
  <CustomerProvider>
    <Header />
    <Main />
  </CustomerProvider>,
);
