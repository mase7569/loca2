package com.localore.localore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;

import java.util.Arrays;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // listen to broadcasts from CreateExerciseService
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        onDBCreated();
                    }
                },
                new IntentFilter(CreateExerciseService.BROADCAST_ACTION));
    }

    public void onCreateDB(View v) {
        Button b = (Button) v;
        b.setText("Loading");
        b.setEnabled(false);

        AppDatabase.getInstance(this).clearAllTables();
        CreateExerciseService.start(this, getWorkingArea());
    }

    public void onDBCreated() {
        Button b = findViewById(R.id.doIt_button);
        b.setText("Done");

//        List<GeoObject> gos = AppDatabase.getInstance(this).geoDao().loadAllGeoObjects();
//
//        for (GeoObject go : gos) {
//            Log.i("_ME_", go.toString());
//        }

        AppDatabase db = AppDatabase.getInstance(this);
        List<Integer> ids = db.geoDao().loadAllGeoObjectIDs();

        for (int id : ids) {
            GeoObject go = db.geoDao().loadGeoObject(id);
            Log.i("_ME_", go.toString());
        }
    }

    /**
     * @return Area of interest. A closed shape.
     */
    private NodeShape getWorkingArea() {
        //uppsala
        // double w = 17.558212280273438;
        // double s = 59.78301472732963;
        // double e = 17.731246948242188;
        // double n = 59.91097597079679;

        //mefjärd
        double w = 18.460774;
        double s = 58.958251;
        double e = 18.619389;
        double n = 59.080544;

        //lidingö
        // double w = 18.08246612548828;
        // double s = 59.33564087770051;
        // double e = 18.27404022216797;
        // double n = 59.39407306645033;

        //rudboda
        // double w = 18.15;
        // double s = 59.372;
        // double e = 18.19;
        // double n = 59.383;

        //new york
        // double w = -74.016259;
        // double s = 40.717569;
        // double e = -73.972399;
        // double n = 40.737473;


        return new NodeShape(Arrays.asList(new double[][]{
                new double[]{w, s},
                new double[]{w, n},
                new double[]{e, n},
                new double[]{e, s}
        }));
    }
}