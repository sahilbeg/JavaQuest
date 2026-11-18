# Security basics with Spring Security

Lock down the Spoke API so strangers can only read stations, riders can see their own rentals, and only ops staff can move bikes.

The controllers are already written. You only change `SecurityConfig.java`.

| Endpoint | Who may call it |
|---|---|
| `GET /stations` | everyone, no login needed |
| `GET /rentals/mine` | anyone who logs in. It answers with the logged-in rider's name. |
| `POST /admin/bikes/{id}/move?to=depot` | only users with the role OPS |

Right now Spring Security is switched off, so every URL is open.

1. **TODO 1:** add `@EnableWebSecurity` above the class.
2. **TODO 2:** put `@Bean` on the `securityFilterChain` method, so Spring uses it.
3. **TODO 3:** inside it, call `http.authorizeHttpRequests(...)`. GET `/stations` is `permitAll()`. `/admin/**` needs `hasRole("OPS")`. `anyRequest()` is `authenticated()`. Keep `anyRequest()` last.
4. **TODO 4:** switch on `httpBasic(Customizer.withDefaults())`, make sessions `SessionCreationPolicy.STATELESS`, and disable `csrf`. Keep `return http.build();` at the end.
5. **TODO 5:** return a `new BCryptPasswordEncoder()` from `passwordEncoder()`.
6. **TODO 6:** in `users(...)`, store two users in the `InMemoryUserDetailsManager`, with hashed passwords:
   - `ada`, password `ride-on`, role `RIDER`
   - `theo`, password `fix-it`, role `OPS`

   Hash each password with `encoder.encode(...)`. Never store the plain text.

You will need a few imports: `EnableWebSecurity`, `HttpMethod`, `Customizer`, `SessionCreationPolicy`, `User` and `BCryptPasswordEncoder`. Your IDE can add them for you.

## How to read the tests

Open `SecurityConfigTest.java`. It starts the whole app with `@SpringBootTest` and sends requests through MockMvc, with the security filter chain in front.

- `.with(httpBasic("ada", "ride-on"))` sends Ada's name and password in a Basic header.
- `@WithMockUser(username = "zoe", roles = "RIDER")` plus `.with(testSecurityContext())` pretends Zoe is already logged in.
- Each test checks a status code: 200 for allowed, 401 for "log in first", 403 for "not allowed".

The tests check that stations stay public, that rentals need a login and that a wrong password gets 401. A rider gets 403 on the admin move, while Kakashi gets 200. No session may be created, and Ada's password must be stored as a BCrypt hash.

Press **Submit** in Cadence, or just save the file in your IDE. The tests run on their own.
