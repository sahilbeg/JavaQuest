package jq.m06.creational;

public record RideTicket(String bikeId, String fromStation, String toStation, String type,
                         int minutes, String riderId, boolean helmet) {

    public static RideTicket ebike(String bikeId, String from, String to, int minutes, String riderId) {
        return builder().bike(bikeId).from(from).to(to).type("ebike").minutes(minutes).rider(riderId).build();
    }

    public static Builder builder() {
        return new Builder();
    }

    public static class Builder {
        private String bikeId;
        private String fromStation;
        private String toStation;
        private String type = "classic";
        private int minutes;
        private String riderId;
        private boolean helmet = false;

        public Builder bike(String bikeId) {
            this.bikeId = bikeId;
            return this;
        }

        public Builder from(String fromStation) {
            this.fromStation = fromStation;
            return this;
        }

        public Builder to(String toStation) {
            this.toStation = toStation;
            return this;
        }

        public Builder type(String type) {
            this.type = type;
            return this;
        }

        public Builder minutes(int minutes) {
            this.minutes = minutes;
            return this;
        }

        public Builder rider(String riderId) {
            this.riderId = riderId;
            return this;
        }

        public Builder helmet(boolean helmet) {
            this.helmet = helmet;
            return this;
        }

        public RideTicket build() {
            if (bikeId == null) {
                throw new IllegalStateException("missing required field: bike");
            }
            if (fromStation == null) {
                throw new IllegalStateException("missing required field: from");
            }
            if (toStation == null) {
                throw new IllegalStateException("missing required field: to");
            }
            if (riderId == null) {
                throw new IllegalStateException("missing required field: rider");
            }
            if (minutes < 0) {
                throw new IllegalStateException("minutes must be 0 or more, got " + minutes);
            }
            return new RideTicket(bikeId, fromStation, toStation, type, minutes, riderId, helmet);
        }
    }
}
