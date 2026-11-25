import { createContext, useContext, useState } from "react";
import type { ReactNode } from "react";

export interface Customer {
  name: string;
  phone: string;
}

export interface CustomerContextValue {
  customer: Customer | null;
  signIn: (customer: Customer) => void;
  signOut: () => void;
}

/** null means "no provider above this component". */
export const CustomerContext = createContext<CustomerContextValue | null>(null);

/** Holds the signed-in customer and shares it with everything inside it. */
export function CustomerProvider({ children }: { children: ReactNode }) {
  const [customer, setCustomer] = useState<Customer | null>(null);
  const value: CustomerContextValue = {
    customer,
    signIn: (next) => setCustomer(next),
    signOut: () => setCustomer(null),
  };
  return <CustomerContext.Provider value={value}>{children}</CustomerContext.Provider>;
}

/** The signed-in customer and the functions to change it. Only works inside <CustomerProvider>. */
export function useCustomer(): CustomerContextValue {
  const value = useContext(CustomerContext);
  if (value === null) throw new Error("useCustomer must be used inside <CustomerProvider>");
  return value;
}
