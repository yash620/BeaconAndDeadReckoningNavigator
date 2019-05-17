package beaconlocalization.dataloader;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;

import beaconlocalization.BeaconScanOperator;

/**
 * Rrepresents a single waypoint
 */
public class Waypoint {
    public List<SingleWaypointData> dataPoints;
    private int x;
    private int y;

    public Waypoint(int x, int y){
        this.x = x;
        this.y = y;
       dataPoints = new ArrayList<>();
    }

    public void addDataPoint(SingleWaypointData data){
        dataPoints.add(data);
    }

    /**
     * @return Map from beacon id to the number of datapoints that beacon appears in
     */
    public HashMap<String, Integer> getBeaconCount(){
        HashMap<String, Integer> counts = new HashMap<>();
        for(SingleWaypointData data : dataPoints){
            for(ConciseIBeacon device : data.devices){
                Integer count = counts.get(device.getId());
                if(count == null){
                    counts.put(device.getId(), 1);
                } else{
                    counts.put(device.getId(), count + 1);
                }

            }
        }

        return counts;
    }

    /**
     * Retrieves a map of the average distances of the beacons associated with this waypoint from
     * the dataset
     * @return - Map of beaconId to average distance
     */
    public HashMap<String, Double> getAverageDistances(){
        HashMap<String, Double> averageDistancesSum = new HashMap<>();
        for(SingleWaypointData data : dataPoints){
            for(ConciseIBeacon device : data.devices){
                Double sum = averageDistancesSum.get(device.getId());
                if(sum == null){
                    averageDistancesSum.put(device.getId(), device.distance);
                } else{
                    averageDistancesSum.put(device.getId(), averageDistancesSum.get(device.getId()) + device.distance);
                }

            }
        }

        HashMap<String, Integer> counts = getBeaconCount();
        HashMap<String, Double> averageDistances = new HashMap<>();
        averageDistancesSum.forEach((id, distanceSum) -> {
            averageDistances.put(id, distanceSum/((double)counts.get(id)));
        });

        return averageDistances;
    }

    /**
     * @return A Hashset of beacon ids that appear percentFoundIn percentage of beacons
     */
    public HashSet<String> getBeaconsInData(){
        HashMap<String, Integer> counts = getBeaconCount();
        HashSet<String> result = new HashSet<>();
        counts.forEach((id, count) -> {
            if(count >= (dataPoints.size() * BeaconScanOperator.PERCENT_FOUND_IN)){
                result.add(id);
            }
        });

        return result;
    }

    public int getX(){
        return x;
    }

    public int getY(){
        return y;
    }

}
