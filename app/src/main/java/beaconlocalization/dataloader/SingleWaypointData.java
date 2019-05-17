package beaconlocalization.dataloader;

import android.util.Pair;

import java.util.HashMap;

/**
 * Corresponds to the data of a single scan from the dataset
 */
public class SingleWaypointData {
    int x;
    int y;

    ConciseIBeacon[] devices;

    public Pair<Integer, Integer> getLocation(){
        return Pair.create(x, y);
    }
}
