package jq.m08.security;

import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/** Ops only: move a bike to another station, for example to the Depot for repairs. */
@RestController
public class AdminController {

    public record BikeMove(String bikeId, String to, String movedBy) { }

    @PostMapping("/admin/bikes/{id}/move")
    public BikeMove move(@PathVariable String id, @RequestParam String to, Authentication auth) {
        // auth is null only while security is switched off (no @EnableWebSecurity yet)
        String who = auth == null ? "nobody" : auth.getName();
        return new BikeMove(id, to, who);
    }
}
