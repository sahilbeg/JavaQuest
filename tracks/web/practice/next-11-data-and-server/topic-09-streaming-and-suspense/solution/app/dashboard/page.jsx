import { Suspense } from "react";
import { getTodaySales, getLowStock } from "../../lib/reports.js";
import { SalesCard, LowStockCard } from "./cards.jsx";

export default function DashboardPage() {
  const salesPromise = getTodaySales();
  const stockPromise = getLowStock();

  return (
    <main>
      <h1>Today at Dhaaga</h1>
      <Suspense fallback={<p>Loading today's sales...</p>}>
        <SalesCard salesPromise={salesPromise} />
      </Suspense>
      <Suspense fallback={<p>Loading low stock...</p>}>
        <LowStockCard stockPromise={stockPromise} />
      </Suspense>
    </main>
  );
}
