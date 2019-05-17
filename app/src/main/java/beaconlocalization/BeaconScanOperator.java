package beaconlocalization;

import android.util.Log;
import android.widget.Toast;

import com.kontakt.sdk.android.ble.configuration.ScanMode;
import com.kontakt.sdk.android.ble.configuration.ScanPeriod;
import com.kontakt.sdk.android.ble.manager.ProximityManager;
import com.kontakt.sdk.android.ble.manager.ProximityManagerFactory;
import com.kontakt.sdk.android.ble.manager.listeners.IBeaconListener;
import com.kontakt.sdk.android.common.profile.IBeaconDevice;
import com.kontakt.sdk.android.common.profile.IBeaconRegion;

import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.ReentrantLock;
import java.util.stream.Collectors;

import beaconlocalization.dataloader.ConciseIBeacon;
import beaconlocalization.dataloader.Waypoint;
import nisargpatel.deadreckoning.activity.GraphActivity;

/**
 * This is the object that will run the scans for the beacons and identifies if a waypoint is found.
 */
public class BeaconScanOperator {
    private HashSet<String> matchedBeacons;
    private HashMap<String, Double> currBeaconsDistances;
    private GraphActivity activity;
    private ProximityManager proximityManager;
    private Waypoint waypointScanningFor;

    private static double DISTANCE_MATCH_DIFFERENCE = 1.5; //Count a beacon as a match for a waypoint if the distance it returns is Abs(expected_distance - actual_distance) < DISTANCE_MATCH_DIFFERENCE
    private static double MATCH_REQUIREMENT = 0.8; //Identify a waypoint as found if this percentage of the beacons in its set have been matched
    public static double  PERCENT_FOUND_IN = 0.7; //Add beacon to a waypoints set if it appears in this percentage of the scans

    private boolean inWaypoint = true;

    int matchCount;
    private HashSet<String> waypointBeacons; //beaconIds associated with the current beacon
    private HashMap<String, Double> waypointBeaconsDistances; //map from the beaconId to its expected distance
    private ReentrantLock lock;

    public BeaconScanOperator(GraphActivity activity){
        this.activity = activity;
        this.proximityManager = setUpProximityManager();
        inWaypoint = false;
        matchedBeacons = new HashSet<>();
        currBeaconsDistances = new HashMap<>();
        lock = new ReentrantLock();
    }

    public void startScanning() {
        //Connect to scanning service and start scanning when ready
        proximityManager.connect(() -> {
            //Check if proximity manager is already scanning
            if (proximityManager.isScanning()) {
                Toast.makeText(activity, "Already scanning", Toast.LENGTH_SHORT).show();
                return;
            }
            proximityManager.startScanning();

            Toast.makeText(activity, "Scanning started", Toast.LENGTH_SHORT).show();
            Log.d("BeaconScanOperator", "Scanning Started");
        });
    }

    public void stopScanning() {
        //Stop scanning if scanning is in progress
        if (proximityManager.isScanning()) {
            proximityManager.stopScanning();
            Toast.makeText(activity, "Scanning stopped", Toast.LENGTH_SHORT).show();
            Log.d("BeaconScanOperator", "Scanning Stopped");
        }
    }

    /**
     * Set the next waypoint that the scanner should be searching for
     * @param w - waypoint object that represents the waypoint being searched for
     */
    public void setWaypointScanningFor(Waypoint w){
        inWaypoint = false;
        waypointScanningFor = w;
        waypointBeacons = w.getBeaconsInData();
        waypointBeaconsDistances = w.getAverageDistances();

        matchedBeacons = new HashSet(matchedBeacons.stream().filter((b) -> {
           return waypointBeacons.contains(b) &&
                   ((Math.abs(waypointBeaconsDistances.get(b) - currBeaconsDistances.get(b)) < DISTANCE_MATCH_DIFFERENCE));
        }).collect(Collectors.toList()));

        Log.d("Waypoint: ", "Searchin for: " + w.getX() + ", " + w.getY());
        Log.d("Random: ", "Already matched: " + matchedBeacons.size());
        Log.d("Random: ", "Size: " + w.dataPoints.size());
        Log.d("Random: ", "Found In All: " + w.getBeaconsInData());
        Log.d("Random: ", "Avg Distances: " + w.getAverageDistances());

        Log.d("BeaconScanOperator", "Scanning for: " + w.getX() + ", " + w.getY());
    }

    private ProximityManager setUpProximityManager(){

        ProximityManager proximityManager = ProximityManagerFactory.create(activity);

        //Configure proximity manager basic options
        proximityManager.configuration()
                //Using ranging for continuous scanning or MONITORING for scanning with intervals
                .scanPeriod(ScanPeriod.RANGING)
                //Using BALANCED for best performance/battery ratio
                .scanMode(ScanMode.BALANCED)
                //OnDeviceUpdate callback will be received with 5 seconds interval
                .deviceUpdateCallbackInterval(TimeUnit.SECONDS.toMillis(5));


        proximityManager.setIBeaconListener(new IBeaconListener() {
            @Override
            public void onIBeaconDiscovered(IBeaconDevice iBeacon, IBeaconRegion region) {
                lock.lock();
                try {
                    String id = (new ConciseIBeacon(iBeacon)).getId();
                    currBeaconsDistances.put(id, iBeacon.getDistance());
                    Log.d("BEACON", "Found beacon: " + iBeacon.getMajor() + ", " + iBeacon.getMinor() + ", Distance: " + iBeacon.getDistance());
                    if (waypointBeacons.contains(id) && !matchedBeacons.contains(id) &&
                            (Math.abs(waypointBeaconsDistances.get(id) - iBeacon.getDistance()) < DISTANCE_MATCH_DIFFERENCE)) {
                        matchedBeacons.add(id);
                    }

                    if (((double) matchedBeacons.size()) >= (((double) waypointBeacons.size()) * MATCH_REQUIREMENT)) {
                        //if (!inWaypoint) {
                            Log.d("FOUND WAYPOINT: ", "Found Waypoint " + waypointScanningFor.getX() + ", " + waypointScanningFor.getY());
                            activity.foundCurrentWaypoint();
                            //inWaypoint = true;
                        //}
                    }
                } finally {
                    lock.unlock();
                }
            }

            @Override
            public void onIBeaconsUpdated(List<IBeaconDevice> iBeacons, IBeaconRegion region) {
                lock.lock();
                try {
                    for (IBeaconDevice iBeacon : iBeacons) {
                        Log.d("BEACON", "Update beacon: " + iBeacon.getMajor() + ", " + iBeacon.getMinor() + ", Distance: " + iBeacon.getDistance());
                        String id = (new ConciseIBeacon(iBeacon)).getId();
                        currBeaconsDistances.put(id, iBeacon.getDistance());
                        if (waypointBeacons.contains(id) && !matchedBeacons.contains(id) &&
                                (Math.abs(waypointBeaconsDistances.get(id) - iBeacon.getDistance()) < DISTANCE_MATCH_DIFFERENCE)) {
                            matchedBeacons.add(id);
                        }

                        if (((double) matchedBeacons.size()) >= (((double) waypointBeacons.size()) * MATCH_REQUIREMENT)) {
                            //if (!inWaypoint) {
                                Log.d("FOUND WAYPOINT: ", "Found Waypoint " + waypointScanningFor.getX() + ", " + waypointScanningFor.getY());
                                activity.foundCurrentWaypoint();
                                //inWaypoint = true;
                            //}
                        }
                    }
                } finally {
                    lock.unlock();
                }

            }

            @Override
            public void onIBeaconLost(IBeaconDevice iBeacon, IBeaconRegion region) {
                lock.lock();
                try {
                    Log.d("BEACON Lost", "Found beacon: " + iBeacon.getMajor() + ", " + iBeacon.getMinor());
                    String id = (new ConciseIBeacon(iBeacon)).getId();
                    currBeaconsDistances.remove(id);
                    matchedBeacons.remove(id);
                } finally {
                    lock.unlock();
                }
            }
        });

        return proximityManager;

    }
}
