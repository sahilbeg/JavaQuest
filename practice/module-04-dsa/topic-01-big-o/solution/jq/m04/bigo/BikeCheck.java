package jq.m04.bigo;

import java.util.List;
import java.util.Set;

public class BikeCheck {

    public static boolean containsSlow(List<String> ids, String id) {
        for (String current : ids) {
            if (current.equals(id)) {
                return true;
            }
        }
        return false;
    }

    public static boolean containsFast(Set<String> ids, String id) {
        return ids.contains(id);
    }
}
