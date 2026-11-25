// next.config.mjs sends these headers with every page: headers() returns [{ source: "/(.*)", headers: securityHeaders }].
// TODO 5: add three { key, value } headers:
//   Content-Security-Policy: the policy from the README, which includes default-src 'self', object-src 'none',
//     base-uri 'self', form-action 'self' and frame-ancestors 'none';
//   X-Content-Type-Options: nosniff;
//   Referrer-Policy: strict-origin-when-cross-origin.
export const securityHeaders = [];
