import { Suspense } from "react";
import { getTodaySales, getLowStock } from "../../lib/reports.js";
import { SalesCard, LowStockCard } from "./cards.jsx";

export default function DashboardPage() {
  // TODO 3: start both reports here, without await:
  // const salesPromise = getTodaySales(); and the same for getLowStock().

  // TODO 4: under the heading, wrap each card in its own boundary:
  // <Suspense fallback={<p>Loading today's sales...</p>}> around <SalesCard salesPromise={salesPromise} />
  // <Suspense fallback={<p>Loading low stock...</p>}> around <LowStockCard stockPromise={stockPromise} />
  return (
    <main>
      <h1>Today at Dhaaga</h1>
    </main>
  );
}
