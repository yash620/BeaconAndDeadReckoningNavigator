package nisargpatel.inertialnavigation.activity;

import android.annotation.TargetApi;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import android.support.v7.app.ActionBarActivity;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import nisargpatel.inertialnavigation.R;
import nisargpatel.inertialnavigation.heading.EulerHeadingInference;
import nisargpatel.inertialnavigation.heading.GyroscopeIntegration;

public class HeadingActivity extends ActionBarActivity implements SensorEventListener{

    private static final float EULER_GYROSCOPE_SENSITIVITY = 0.0025f;
    private static final float GYROSCOPE_SENSITIVITY = 0f;

    private EulerHeadingInference eulerHeadingInference;
    private GyroscopeIntegration gyroscopeIntegration;
    private GyroscopeIntegration gyroscopeIntegrationEuler;

    private Sensor gyroscopeCalibrated;
    private Sensor gyroscopeUncalibrated;
    private Sensor rotationVector;
    private Sensor geoRotationVector;
    private Sensor gameRotationVector;
    private SensorManager sm;

    private TextView textGyroscopeMatrix;
    private TextView textGyroscope;
    private TextView textRotation;
    private TextView textGeoRotation;
    private TextView textGameRotation;

    private double gyroHeading;

    @TargetApi(Build.VERSION_CODES.KITKAT)
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_heading);

        eulerHeadingInference = new EulerHeadingInference();
        gyroscopeIntegration = new GyroscopeIntegration(GYROSCOPE_SENSITIVITY, null);
        gyroscopeIntegrationEuler = new GyroscopeIntegration( EULER_GYROSCOPE_SENSITIVITY, null);

        gyroHeading = 0;

        textGyroscopeMatrix = (TextView) findViewById(R.id.textGyroscopeMatrix);
        textGyroscope = (TextView) findViewById(R.id.textGyroscope);
        textRotation = (TextView) findViewById(R.id.textRotation);
        textGeoRotation = (TextView) findViewById(R.id.textGeoRotation);
        textGameRotation = (TextView) findViewById(R.id.textGameRotation);

        Button buttonStart = (Button) findViewById(R.id.buttonStart);
        Button buttonStop = (Button) findViewById(R.id.buttonStop);

        sm = (SensorManager) getSystemService(SENSOR_SERVICE);

        gyroscopeUncalibrated = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE_UNCALIBRATED);
        gyroscopeCalibrated = sm.getDefaultSensor(Sensor.TYPE_GYROSCOPE);
        rotationVector = sm.getDefaultSensor(Sensor.TYPE_ROTATION_VECTOR);
        geoRotationVector = sm.getDefaultSensor(Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR);
        gameRotationVector = sm.getDefaultSensor(Sensor.TYPE_GAME_ROTATION_VECTOR);

        buttonStart.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sm.registerListener(HeadingActivity.this, gyroscopeUncalibrated, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(HeadingActivity.this, gyroscopeCalibrated, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(HeadingActivity.this, rotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(HeadingActivity.this, geoRotationVector, SensorManager.SENSOR_DELAY_FASTEST);
                sm.registerListener(HeadingActivity.this, gameRotationVector, SensorManager.SENSOR_DELAY_FASTEST);
            }
        });

        buttonStop.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                sm.unregisterListener(HeadingActivity.this, gyroscopeUncalibrated);
                sm.unregisterListener(HeadingActivity.this, gyroscopeCalibrated);
                sm.unregisterListener(HeadingActivity.this, rotationVector);
                sm.unregisterListener(HeadingActivity.this, geoRotationVector);
                sm.unregisterListener(HeadingActivity.this, gameRotationVector);
            }
        });

        findViewById(R.id.buttonClear).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                textGyroscopeMatrix.setText("0");
                textGyroscope.setText("0");
                textRotation.setText("0");
                textGeoRotation.setText("0");
                textGameRotation.setText("0");

                eulerHeadingInference.clearMatrix();

                gyroHeading = 0;
            }
        });

    }

    @Override
    public void onAccuracyChanged(Sensor sensor, int accuracy) {}

    @Override
    public void onSensorChanged(SensorEvent event) {

        int sensorType = event.sensor.getType();

        if (sensorType == Sensor.TYPE_GYROSCOPE_UNCALIBRATED) {

            float[] deltaOrientation = gyroscopeIntegrationEuler.getIntegratedValues(event.timestamp, event.values);

            float eulerHeading = eulerHeadingInference.getCurrentHeading(deltaOrientation);
            textGyroscopeMatrix.setText(String.valueOf(eulerHeading));

        } else if (sensorType == Sensor.TYPE_GYROSCOPE) {

            float[] deltaOrientation = gyroscopeIntegration.getIntegratedValues(event.timestamp, event.values);

            gyroHeading += deltaOrientation[2];
            textGyroscope.setText(String.valueOf(gyroHeading));

        } else if (sensorType == Sensor.TYPE_ROTATION_VECTOR) {
            textRotation.setText(String.valueOf(event.values[2]));
        } else if (sensorType == Sensor.TYPE_GEOMAGNETIC_ROTATION_VECTOR) {
            textGeoRotation.setText(String.valueOf(event.values[2]));
        } else if (sensorType == Sensor.TYPE_GAME_ROTATION_VECTOR) {
            textGameRotation.setText(String.valueOf(event.values[2]));
        }
    }



}