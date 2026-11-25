/** Runs before the routes in config.matcher. Return a Response to stop the request, or nothing to let it through. */
export function proxy(request) {
  const { pathname } = request.nextUrl;

  // TODO 1: /catalogue and every path under it moved for good to /items.
  // Return Response.redirect(new URL(`/items${rest}`, request.url), 308), where rest is what comes after /catalogue.

  // TODO 2: /account and every path under it need a dhaaga_session cookie: request.cookies.get("dhaaga_session")?.value.
  // Without one, return Response.redirect(new URL(`/login?next=${pathname}`, request.url), 307).
}

export const config = {
  // TODO 3: run only for "/account/:path*" and "/catalogue/:path*".
  matcher: [],
};
