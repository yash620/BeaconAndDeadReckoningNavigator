package beaconlocalization.dataloader;

import com.kontakt.sdk.android.common.profile.IBeaconDevice;

public class ConciseIBeacon {
    int major;
    int minor;

    double distance;

    public ConciseIBeacon(IBeaconDevice device){
        major = device.getMajor();
        minor = device.getMinor();
        distance = device.getDistance();
    }

    public String getId(){
        return Integer.toString(major) + " " +
                Integer.toString(minor);
    }
}
