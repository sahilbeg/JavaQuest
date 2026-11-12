package jq.m06.structural;

public interface Lock {

    boolean open(String bikeId);

    boolean close(String bikeId);
}
