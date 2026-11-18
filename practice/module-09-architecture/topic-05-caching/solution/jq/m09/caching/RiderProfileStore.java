package jq.m09.caching;

import java.util.Optional;

/** The slow rider-profile service: the source of truth. Given to you. */
public interface RiderProfileStore {

    /** The rider's profile, or Optional.empty() when no rider has this id. */
    Optional<RiderProfile> load(String riderId);

    /** Creates or replaces the profile with profile.riderId(). */
    void save(RiderProfile profile);
}
