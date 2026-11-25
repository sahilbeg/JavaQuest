"use server";

import { cookies } from "next/headers";
import { redirect } from "next/navigation";
import { startSession, endSession } from "../../lib/session.js";

// This file is done. It shows how the app passes the real cookie store to session.js.
// (Checking the one-time code sent to the phone is left out to keep the example short.)

export async function logIn(formData) {
  const phone = String(formData.get("phone") ?? "").replace(/\s/g, "");
  startSession(await cookies(), phone);
  redirect("/account");
}

export async function logOut() {
  endSession(await cookies());
  redirect("/");
}
