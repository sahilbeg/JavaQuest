package jq.m08.security;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;
import static org.junit.jupiter.api.Assertions.assertTrue;
import static org.junit.jupiter.api.Assertions.fail;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.httpBasic;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.testSecurityContext;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;

import com.jayway.jsonpath.JsonPath;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.webmvc.test.autoconfigure.AutoConfigureMockMvc;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;

@SpringBootTest
@AutoConfigureMockMvc
class SecurityConfigTest {

    @Autowired MockMvc mvc;
    @Autowired UserDetailsService users;
    @Autowired PasswordEncoder encoder;

    @Test
    @DisplayName("Anyone can list the stations without logging in")
    void stationsArePublic() throws Exception {
        MvcResult r = mvc.perform(get("/stations")).andReturn();
        assertEquals(200, r.getResponse().getStatus(),
                "GET /stations should answer 200 with no credentials. Did you permitAll() GET /stations?");
    }

    @Test
    @DisplayName("My rentals without credentials answers 401 Unauthorized")
    void myRentalsNeedLogin() throws Exception {
        MvcResult r = mvc.perform(get("/rentals/mine")).andReturn();
        assertEquals(401, r.getResponse().getStatus(),
                "GET /rentals/mine with no credentials should answer 401. You need @EnableWebSecurity, a @Bean filter chain,"
                        + " anyRequest().authenticated() and httpBasic(). A 403 here means httpBasic() is missing.");
    }

    @Test
    @DisplayName("Ada logs in with Basic auth and sees her own rentals")
    void adaSeesHerRentals() throws Exception {
        MvcResult r = mvc.perform(get("/rentals/mine").with(httpBasic("ada", "ride-on"))).andReturn();
        assertEquals(200, r.getResponse().getStatus(),
                "ada with password ride-on should get 200 from /rentals/mine. Is ada in your InMemoryUserDetailsManager?");
        String rider = JsonPath.read(r.getResponse().getContentAsString(), "$.rider");
        assertEquals("ada", rider,
                "The rentals should belong to the logged-in rider, ada. Is Spring Security switched on with @EnableWebSecurity?");
    }

    @Test
    @DisplayName("A wrong password answers 401 Unauthorized")
    void wrongPasswordIsRejected() throws Exception {
        MvcResult r = mvc.perform(get("/rentals/mine").with(httpBasic("ada", "ride-off"))).andReturn();
        assertEquals(401, r.getResponse().getStatus(),
                "ada with the wrong password should get 401, because Spring Security cannot prove who she is");
    }

    @Test
    @DisplayName("A rider who tries to move a bike gets 403 Forbidden")
    void riderCannotMoveBikes() throws Exception {
        MvcResult r = mvc.perform(post("/admin/bikes/B-1042/move").param("to", "depot")
                .with(httpBasic("ada", "ride-on"))).andReturn();
        assertEquals(403, r.getResponse().getStatus(),
                "ada is a RIDER, so POST /admin/bikes/B-1042/move should answer 403. Use hasRole(\"OPS\") on /admin/**.");
    }

    @Test
    @DisplayName("Moving a bike with no credentials answers 401 Unauthorized")
    void anonymousCannotMoveBikes() throws Exception {
        MvcResult r = mvc.perform(post("/admin/bikes/B-1042/move").param("to", "depot")).andReturn();
        assertEquals(401, r.getResponse().getStatus(),
                "With no credentials at all, the admin endpoint should answer 401: the API does not know who you are yet."
                        + " A 403 here usually means csrf is still switched on.");
    }

    @Test
    @DisplayName("Theo from ops can move a bike to the Depot")
    void opsCanMoveBikes() throws Exception {
        MvcResult r = mvc.perform(post("/admin/bikes/B-1042/move").param("to", "depot")
                .with(httpBasic("theo", "fix-it"))).andReturn();
        assertEquals(200, r.getResponse().getStatus(),
                "theo has role OPS, so the move should answer 200. A 403 here often means csrf is still switched on.");
        String movedBy = JsonPath.read(r.getResponse().getContentAsString(), "$.movedBy");
        assertEquals("theo", movedBy,
                "The move should record theo as the person who moved the bike. Is Spring Security switched on?");
    }

    @Test
    @DisplayName("A refused request does not create a session, because the API is stateless")
    void noSessionIsCreated() throws Exception {
        MvcResult r = mvc.perform(get("/rentals/mine")).andReturn();
        assertEquals(401, r.getResponse().getStatus(), "GET /rentals/mine with no credentials should answer 401");
        assertNull(r.getRequest().getSession(false),
                "A stateless API should never create a session. Set sessionCreationPolicy(SessionCreationPolicy.STATELESS).");
    }

    @Test
    @WithMockUser(username = "zoe", roles = "RIDER")
    @DisplayName("A pretend rider from @WithMockUser can read their rentals but cannot move bikes")
    void mockUserIsARider() throws Exception {
        // testSecurityContext() carries the @WithMockUser user into each MockMvc request
        MvcResult mine = mvc.perform(get("/rentals/mine").with(testSecurityContext())).andReturn();
        assertEquals(200, mine.getResponse().getStatus(), "zoe is logged in, so /rentals/mine should answer 200");
        String rider = JsonPath.read(mine.getResponse().getContentAsString(), "$.rider");
        assertEquals("zoe", rider,
                "The rentals should belong to the logged-in rider, zoe. Is Spring Security switched on?");
        MvcResult move = mvc.perform(post("/admin/bikes/B-1042/move").param("to", "depot")
                .with(testSecurityContext())).andReturn();
        assertEquals(403, move.getResponse().getStatus(), "zoe is a RIDER, not OPS, so the move should answer 403");
    }

    @Test
    @DisplayName("Ada's password is stored as a hash, not as plain text")
    void passwordIsHashed() {
        UserDetails ada;
        try {
            ada = users.loadUserByUsername("ada");
        } catch (UsernameNotFoundException e) {
            fail("The UserDetailsService has no user called ada yet. Add her in the users() bean.");
            return;
        }
        assertNotEquals("ride-on", ada.getPassword(),
                "The stored password must not be the plain text ride-on. Use BCryptPasswordEncoder and encoder.encode(...).");
        assertTrue(ada.getPassword().startsWith("$2"),
                "A BCrypt hash starts with $2. Return new BCryptPasswordEncoder() from passwordEncoder().");
        assertTrue(encoder.matches("ride-on", ada.getPassword()),
                "encoder.matches(\"ride-on\", storedHash) should be true, so ada can still log in");
    }
}
