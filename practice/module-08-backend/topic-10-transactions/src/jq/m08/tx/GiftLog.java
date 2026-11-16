package jq.m08.tx;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.Id;

/** One row for every gift that went through, so finance can check the money later. */
@Entity
public class GiftLog {

    @Id
    @GeneratedValue
    private Long id;

    private String fromRider;

    private String toRider;

    private long cents;

    protected GiftLog() {
        // JPA needs a no-argument constructor
    }

    public GiftLog(String fromRider, String toRider, long cents) {
        this.fromRider = fromRider;
        this.toRider = toRider;
        this.cents = cents;
    }

    public Long getId() {
        return id;
    }

    public String getFromRider() {
        return fromRider;
    }

    public String getToRider() {
        return toRider;
    }

    public long getCents() {
        return cents;
    }
}
