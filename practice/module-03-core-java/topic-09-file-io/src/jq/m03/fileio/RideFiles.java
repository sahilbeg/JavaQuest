package jq.m03.fileio;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.util.ArrayList;
import java.util.List;

public class RideFiles {

    public static List<Ride> read(File file) throws IOException {
        List<Ride> rides = new ArrayList<>();
        // TODO 1: open a BufferedReader around new FileReader(file, StandardCharsets.UTF_8) inside try ( ... )
        // TODO 2: call readLine() in a loop until it returns null, and skip blank lines
        // TODO 3: split each line on "," and add a new Ride (use Integer.parseInt for the minutes)
        return rides;
    }

    public static void write(File file, List<Ride> rides) throws IOException {
        // TODO 4: open a BufferedWriter around new FileWriter(file, StandardCharsets.UTF_8) inside try ( ... )
        // TODO 5: for each ride, write "bikeId,station,type,minutes" and then call newLine()
    }

    public static int countLines(File file) throws IOException {
        // TODO 6: read the file line by line and count every line, blank ones too
        return 0;
    }
}
