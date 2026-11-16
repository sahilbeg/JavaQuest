package jq.m08.tx;

import jakarta.persistence.Entity;
import jakarta.persistence.Id;

/** A rider's ride credit, in cents. One row per rider. */
@Entity
public class Wallet {

    @Id
    private String riderId;     // "r-17"

    private long balanceCents;

    protected Wallet() {
        // JPA needs a no-argument constructor
    }

    public Wallet(String riderId, long balanceCents) {
        this.riderId = riderId;
        this.balanceCents = balanceCents;
    }

    public String getRiderId() {
        return riderId;
    }

    public long getBalanceCents() {
        return balanceCents;
    }

    public void withdraw(long cents) {
        balanceCents -= cents;
    }

    public void deposit(long cents) {
        balanceCents += cents;
    }
}
