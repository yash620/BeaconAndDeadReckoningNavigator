package beaconlocalization.dataloader;

import android.Manifest;
import android.app.Activity;
import android.content.pm.PackageManager;
import android.support.v4.app.ActivityCompat;
import android.util.Log;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStreamReader;

/**
 * Retrieves data from a file line by line
 */
public class DataReader {
    private String fileName;
    private FileInputStream is;
    private BufferedReader reader;

    public DataReader(String fileName){
        this.fileName = fileName;
        File file = new File(getAbsoluteFileName());

        if(file.exists()){
            try {
                is = new FileInputStream(file);
            } catch (FileNotFoundException e) {
                e.printStackTrace();
            }
            reader = new BufferedReader(new InputStreamReader(is));
        } else {
            Log.e("File Read Error: ", "File: " + getAbsoluteFileName() + " doesn't exist");
        }
    }

    public String readLine() throws IOException {
       return reader.readLine();
    }

    public void close(){
        try {
            is.close();;
            reader.close();
        } catch (IOException e) {
            e.printStackTrace();
            Log.e("File Read Error:", "Failed to close streams");
        }
    }


    private String getAbsoluteFileName(){
        File SDFile = android.os.Environment.getExternalStorageDirectory();
        Log.d("balh", "File Seperator: " + File.separator);
        Log.d("balh", "Absolute Path: " + SDFile.getAbsolutePath());
        return SDFile.getAbsolutePath() + File.separator + fileName;
    }

    public static void verifyStoragePermissions(Activity activity) {
        // Check if we have write permission
        int permission = ActivityCompat.checkSelfPermission(activity, Manifest.permission.READ_EXTERNAL_STORAGE);

        if (permission != PackageManager.PERMISSION_GRANTED) {
            // We don't have permission so prompt the user
            ActivityCompat.requestPermissions(
                    activity,
                    new String[]{Manifest.permission.WRITE_EXTERNAL_STORAGE,
                            Manifest.permission.READ_EXTERNAL_STORAGE},
                    1
            );
        }
    }

}
