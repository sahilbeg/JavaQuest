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
  // TODO 1: keep the customer in useState<Customer | null>(null), and wrap children in
  //         <CustomerContext.Provider value={{ customer, signIn, signOut }}>.
  //         signIn stores the customer it is given; signOut sets the customer back to null.
  return <>{children}</>;
}

/** The signed-in customer and the functions to change it. Only works inside <CustomerProvider>. */
export function useCustomer(): CustomerContextValue {
  // TODO 2: read the context with useContext(CustomerContext). If it is null, throw
  //         new Error("useCustomer must be used inside <CustomerProvider>"). Otherwise return it.
  return { customer: null, signIn: () => {}, signOut: () => {} };
}
