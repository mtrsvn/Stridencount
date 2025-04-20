package com.example.stridencount;

import android.Manifest;
import android.content.pm.PackageManager;
import android.hardware.Sensor;
import android.hardware.SensorEvent;
import android.hardware.SensorEventListener;
import android.hardware.SensorManager;
import android.os.Build;
import android.os.Bundle;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import android.util.Log;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

public class StrideCount extends AppCompatActivity implements SensorEventListener {
    private TextView distTxt;
    private TextView stepsTxt;
    private TextView calTxt;
    private Button rstBtn;

    private SensorManager senMgr;
    private Sensor stepSen;

    private int steps = 0;
    private double dist = 0.0;
    private double cals = 0.0;

    private static final double STEP_LEN = 0.78;
    private static final double CAL_PER_M = 0.05;

    private static final int PERM_REQ_CODE = 1;

    private int initSteps = 0;
    private boolean initStepsSet = false;

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_stride_count);

        String name = getIntent().getStringExtra("name");
        TextView greetingText = findViewById(R.id.helloworld);
        if (greetingText != null) {
            greetingText.setText("Hello, " + name);
        }

        distTxt = findViewById(R.id.walkingDistance);
        stepsTxt = findViewById(R.id.stepCount);
        calTxt = findViewById(R.id.caloriesBurned);
        rstBtn = findViewById(R.id.resetButton);
        senMgr = (SensorManager) getSystemService(SENSOR_SERVICE);
        stepSen = senMgr.getDefaultSensor(Sensor.TYPE_STEP_COUNTER);

        if (distTxt == null || stepsTxt == null || calTxt == null || rstBtn == null) {
            Log.e("StrideCount", "One or more TextViews/Buttons are not properly initialized.");
            return;
        }

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.Q) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.ACTIVITY_RECOGNITION) != PackageManager.PERMISSION_GRANTED) {
                ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACTIVITY_RECOGNITION}, PERM_REQ_CODE);
            } else {
                regStepSen();
            }
        } else {
            regStepSen();
        }

        rstBtn.setOnClickListener(v -> rst());
    }

    private void regStepSen() {
        if (stepSen != null) {
            senMgr.registerListener(this, stepSen, SensorManager.SENSOR_DELAY_UI);
        } else {
            Log.e("StrideCount", "Step Counter sensor not available.");
        }
    }

    public void onSensorChanged(SensorEvent event) {
        if (event.sensor.getType() == Sensor.TYPE_STEP_COUNTER) {
            if (!initStepsSet) {
                initSteps = (int) event.values[0];
                initStepsSet = true;
            }
            steps = (int) event.values[0] - initSteps;
            dist = steps * STEP_LEN / 1000;
            cals = steps * STEP_LEN * CAL_PER_M;

            distTxt.setText(String.format("%.2f", dist) + "KM");
            stepsTxt.setText(String.valueOf(steps));
            calTxt.setText(String.format("%.2f", cals) + "KCAL");
        }
    }

    public void onAccuracyChanged(Sensor sensor, int accuracy) {
    }

    protected void onDestroy() {
        super.onDestroy();
        senMgr.unregisterListener(this);
    }

    private void rst() {
        initSteps = steps + initSteps;
        steps = 0;
        dist = 0.0;
        cals = 0.0;

        distTxt.setText("0.00KM");
        stepsTxt.setText("0");
        calTxt.setText("0.00KCAL");
        Toast.makeText(getApplicationContext(), "Statistics have been successfully reset", Toast.LENGTH_SHORT).show();
    }
}
