package jq.m06.creational;

public record RideTicket(String bikeId, String fromStation, String toStation, String type,
                         int minutes, String riderId, boolean helmet) {

    public static RideTicket ebike(String bikeId, String from, String to, int minutes, String riderId) {
        // TODO 6: build an "ebike" ticket with the builder and return it.
        return new RideTicket(bikeId, to, from, "classic", minutes, riderId, false);
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        // TODO 1: add a private field for each of the seven values.
        // type starts as "classic" and helmet starts as false.

        // TODO 2: make every setter store its value and keep returning this.
        public Builder bike(String bikeId) {
            return this;
        }

        public Builder from(String fromStation) {
            return this;
        }

        public Builder to(String toStation) {
            return this;
        }

        public Builder type(String type) {
            return this;
        }

        public Builder minutes(int minutes) {
            return this;
        }

        public Builder rider(String riderId) {
            return this;
        }

        public Builder helmet(boolean helmet) {
            return this;
        }

        public RideTicket build() {
            // TODO 3: check bike, from, to and rider in that order.
            //         Throw new IllegalStateException("missing required field: bike") for the first one missing.
            // TODO 4: throw an IllegalStateException when minutes is below 0.
            // TODO 5: create the RideTicket from the fields.
            return new RideTicket("", "", "", "", 0, "", false);
        }
    }
}
