package jq.m10.capstone;

/**
 * Core (domain): every way a rental request can break a business rule.
 * The class is sealed, so this file lists them all. The core knows nothing about HTTP:
 * a web adapter decides later which status code each one becomes.
 */
public abstract sealed class RentalException extends RuntimeException {

    private RentalException(String message) {
        super(message);
    }

    /** No rental with this id in this city. */
    public static final class RentalNotFound extends RentalException {
        private final String rentalId;

        public RentalNotFound(String rentalId) {
            super("No rental " + rentalId);
            this.rentalId = rentalId;
        }

        public String rentalId() {
            return rentalId;
        }
    }

    /** The station is not one of this city's stations. */
    public static final class StationNotFound extends RentalException {
        private final String stationId;

        public StationNotFound(String stationId, String cityId) {
            super("No station " + stationId + " in " + cityId);
            this.stationId = stationId;
        }

        public String stationId() {
            return stationId;
        }
    }

    /** No bike with this id in this city. A bike from another city counts as not found. */
    public static final class BikeNotFound extends RentalException {
        private final String bikeId;

        public BikeNotFound(String bikeId, String cityId) {
            super("No bike " + bikeId + " in " + cityId);
            this.bikeId = bikeId;
        }

        public String bikeId() {
            return bikeId;
        }
    }

    /** The city does not rent out this type of bike, such as CARGO in Bramford. */
    public static final class BikeTypeNotOffered extends RentalException {
        private final BikeType type;

        public BikeTypeNotOffered(BikeType type, String cityId) {
            super(cityId + " does not offer " + type + " bikes");
            this.type = type;
        }

        public BikeType type() {
            return type;
        }
    }

    /** The bike is out on a ride, or not docked at the station the rider is standing at. */
    public static final class BikeUnavailable extends RentalException {
        private final String bikeId;

        public BikeUnavailable(String bikeId, String stationId) {
            super(bikeId + " is not available at " + stationId);
            this.bikeId = bikeId;
        }

        public String bikeId() {
            return bikeId;
        }
    }

    /** The rider is already on a ride. One rider, one bike at a time. */
    public static final class ActiveRentalExists extends RentalException {
        private final String riderId;

        public ActiveRentalExists(String riderId, String activeRentalId) {
            super(riderId + " is already riding in " + activeRentalId);
            this.riderId = riderId;
        }

        public String riderId() {
            return riderId;
        }
    }

    /** The rental has already ended. Ending it again would charge twice. */
    public static final class RentalAlreadyEnded extends RentalException {
        private final String rentalId;

        public RentalAlreadyEnded(String rentalId) {
            super(rentalId + " has already ended");
            this.rentalId = rentalId;
        }

        public String rentalId() {
            return rentalId;
        }
    }

    /** The rider sent an idempotency key again, but with a different bike or station. That is a bug, not a retry. */
    public static final class IdempotencyKeyReused extends RentalException {
        private final String idempotencyKey;

        public IdempotencyKeyReused(String idempotencyKey) {
            super("Idempotency key " + idempotencyKey + " was already used for a different request");
            this.idempotencyKey = idempotencyKey;
        }

        public String idempotencyKey() {
            return idempotencyKey;
        }
    }
}
