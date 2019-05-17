package beaconlocalization.dataloader;

import android.util.Log;

import com.google.gson.Gson;

import java.io.IOException;
import java.util.HashMap;
import android.util.Pair;

/**
 * Retreies the waypoints from the data set file
 */
public class WaypointDataLoader {
    private String dataFileName;

    public WaypointDataLoader(String dataFileName){
        this.dataFileName = dataFileName;
    }

    /**
     * Loads waypoints from the files
     * @return Map from Pair(x-coordinate, y-coordinate) to the associated Waypoint object
     */
    public HashMap<Pair<Integer, Integer>, Waypoint> loadWaypoints(){
        DataReader reader = new DataReader(dataFileName);
        Gson gson = new Gson();
        HashMap<Pair<Integer, Integer>, Waypoint> waypoints = new HashMap<>();
        try {
            String line = reader.readLine();
            while(line != null){
                SingleWaypointData curr = gson.fromJson(line, SingleWaypointData.class);
                if(waypoints.containsKey(curr.getLocation())){
                    waypoints.get(curr.getLocation()).addDataPoint(curr);
                } else {
                    Waypoint toAdd = new Waypoint(curr.x, curr.y);
                    toAdd.addDataPoint(curr);
                    waypoints.put(curr.getLocation(), toAdd);
                    Log.d("SingleWaypointData", "Adding waypoint: " + curr.getLocation());
                }
                line = reader.readLine();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }

        reader.close();

        return waypoints;
    }
}

