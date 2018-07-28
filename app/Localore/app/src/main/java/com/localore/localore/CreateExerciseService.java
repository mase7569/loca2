package com.localore.localore;

import android.app.IntentService;
import android.app.Notification;
import android.app.NotificationChannel;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.content.Intent;
import android.content.Context;
import android.os.Build;
import android.support.v4.app.NotificationCompat;
import android.support.v4.content.LocalBroadcastManager;
import android.util.Log;

import com.localore.localore.model.AppDatabase;
import com.localore.localore.model.Exercise;
import com.localore.localore.model.ExerciseCreation;
import com.localore.localore.model.NodeShape;


/**
 * Service for completing exercise-construction by updating the database.
 * Fetches OSM-geo-elements using Overpass service, processes these into GeoObjects and
 * constructs quizzes.
 *
 * In: An exercise with name and working area.
 * - Adds geo-objects of this exercise to the database.
 * - Completes exercise with predefined quizzes.
 */
public class CreateExerciseService extends IntentService {
    private static final String EXERCISE_ID_PARAM_KEY = "com.localore.localore.CreateExerciseService.EXERCISE_PARAM_KEY";
    public static final String BROADCAST_ACTION = "com.localore.localore.CreateExerciseService.BROADCAST_ACTION";
    public static final String REPORT_KEY = "com.localore.localore.CreateExerciseService.REPORT_KEY";

    public CreateExerciseService() {
        super("CreateExerciseService");
    }

    /**
     * Starts the service and passes parameter exercise-id.
     *
     * @param context
     * @param exerciseId Id of exercise (currently in AppDatabase!) to be created completely.
     */
    public static void start(Context context, long exerciseId) {
        Intent intent = new Intent(context, CreateExerciseService.class);
        intent.putExtra(EXERCISE_ID_PARAM_KEY, exerciseId);
        context.startService(intent);
    }

    @Override
    public int onStartCommand(Intent intent, int flags, int startId) {
        // request foreground
        Intent notificationIntent = new Intent(this, MainActivity.class);
        notificationIntent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK | Intent.FLAG_ACTIVITY_SINGLE_TOP);
        PendingIntent pendingIntent = PendingIntent.getActivity(this, 0, notificationIntent, 0);

        createNotificationChannel();
        Notification notification = new NotificationCompat.Builder(this, "default_channel_id")
                .setSmallIcon(R.drawable.loca_notification_icon)
                .setContentTitle("Creating new exercise...")
                //.setContentText(textContent)
                .setPriority(NotificationCompat.PRIORITY_LOW)
                .setContentIntent(pendingIntent)
                .build();

        int NOTIFICATION_ID = 1;
        startForeground(NOTIFICATION_ID, notification);

        return super.onStartCommand(intent, flags, startId);
    }

    // required for Android 8.0 and higher
    private void createNotificationChannel() {
        // Create the NotificationChannel, but only on API 26+ because
        // the NotificationChannel class is new and not in the support library
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
            CharSequence name = "whatever";
            String description = "whatever";
            int importance = NotificationManager.IMPORTANCE_LOW;
            NotificationChannel channel = new NotificationChannel("default_channel_id", name, importance);
            channel.setDescription(description);
            NotificationManager notificationManager = getSystemService(NotificationManager.class);
            notificationManager.createNotificationChannel(channel);
        }
    }

    @Override
    protected void onHandleIntent(Intent intent) {
        Log.i("_ME_", "CreateExerciseService started");

        if (intent == null) return;
        long exerciseId = intent.getLongExtra(EXERCISE_ID_PARAM_KEY, -1);
        AppDatabase db = AppDatabase.getInstance(this);

        Exercise exercise = db.exerciseDao().load(exerciseId);
        if (exercise == null) throw new RuntimeException("Exercise not in db");

        NodeShape workingArea = exercise.getWorkingArea();
        boolean ok = ExerciseCreation.acquireGeoObjects(workingArea, this);
        if (!ok) {
            report("Failed");
            return;
        }

        Log.d("_ME_", "Post processing");
        ExerciseCreation.postProcessing(exercise, this);

        report("Done!");
    }

    @Override
    public void onDestroy() {
        Log.d("_ME_", "CreateExerciseService destroyed");
        super.onDestroy();
    }

    /**
     * Broadcast status-report.
     * @param report
     */
    private void report(String report) {
        Intent localIntent = new Intent(BROADCAST_ACTION);
        localIntent.putExtra(REPORT_KEY, report);
        LocalBroadcastManager.getInstance(this).sendBroadcast(localIntent);
    }
}
