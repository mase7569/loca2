package com.localore.localore;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.support.v4.content.LocalBroadcastManager;
import android.support.v7.app.ActionBar;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.TextView;

import com.localore.localore.model.AppDatabase;
import com.localore.localore.model.NodeShape;
import com.localore.localore.model.Session;
import com.localore.localore.model.SessionDao;
import com.localore.localore.modelManipulation.SessionControl;

import org.w3c.dom.Node;

import java.util.zip.Adler32;

/**
 * Singleton-activity that handles loading of a new exercise.
 *
 * Before start:
 *  - New exercise name and working-area stored in Session (in db).
 *  - Set loading-exercise's not-started-status in Session for a fresh start of the loading-process.
 *
 *  Before leave:
 *   - Set exercise-creation's not-started-status in Session (restarted).
 */
public class LoadingNewExerciseActivity extends AppCompatActivity {

    private TextView textView_loadingStatus;
    private Button button_enterOrRetry;

    //region status-codes
    public static final int NOT_STARTED = 0;
    public static final int RUNNING = 1;
    public static final int COMPLETED = 2;
    public static final int UNSPECIFIED_ERROR = -1;
    public static final int NETWORK_ERROR = -2;
    public static final int TOO_FEW_GEO_OBJECTS_ERROR = -3;
    public static final int DEAD_SERVICE_ERROR = -4;
    //endregion

    /**
     * Use this start loading a new exercise (as opposed to resuming progress).
     * @param name
     * @param workingArea
     */
    public static void freshStart(String name, NodeShape workingArea, Context context) {
        SessionControl.initLoadingOfNewExercise(name, workingArea, AppDatabase.getInstance(context));
        Intent intent = new Intent(context, LoadingNewExerciseActivity.class);
        context.startActivity(intent);
    }

    /**
     * @param savedInstanceState
     */
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_loading_new_exercise);

        this.textView_loadingStatus = findViewById(R.id.textView_loadingStatus);
        this.button_enterOrRetry = findViewById(R.id.button_enterOrRetry);

        setTitle(getString(R.string.loading_new_exercise));
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) actionBar.hide();

        // listen to broadcasts from CreateExerciseService
        LocalBroadcastManager.getInstance(this).registerReceiver(
                new BroadcastReceiver() {
                    @Override
                    public void onReceive(Context context, Intent intent) {
                        statusBasedControlFlow();
                    }
                },
                new IntentFilter(CreateExerciseService.BROADCAST_ACTION)
        );

        validateLoadingExerciseStatus();
        statusBasedControlFlow();
    }

    /**
     * If Session-status says Working, but service isn't running, the service has been
     * prematurely destroyed. If so, Session-status is set to an error.
     */
    private void validateLoadingExerciseStatus() {
        AppDatabase db = AppDatabase.getInstance(this);
        int status = SessionControl.load(db).getLoadingExerciseStatus();

        if (status == RUNNING && !createExerciseServiceIsRunning())
            SessionControl.updateLoadingExerciseStatus(DEAD_SERVICE_ERROR, db);
    }

    /**
     * @return True if the create-exercise-service is running.
     */
    private boolean createExerciseServiceIsRunning() {
        return CreateExerciseService.start(null, null, this) != null;
    }

    /**
     * Delegates layout-work based on current status on construction.
     * Status read from Session in db.
     *
     * If error-status: also cleans up database by wiping the new data.
     */
    private void statusBasedControlFlow() {
        //SessionDao sessionDao = AppDatabase.getInstance(this).sessionDao();
        Session session = AppDatabase.getInstance(this).sessionDao().load();
        int status = session.getLoadingExerciseStatus();

        if (status == NOT_STARTED) {
            String name = session.getLoadingExerciseName();
            NodeShape workingArea = session.getLoadingExerciseWorkingArea();
            SessionControl.updateLoadingExerciseStatus(RUNNING, AppDatabase.getInstance(this));

            CreateExerciseService.start(name, workingArea, this);
            workingLayout();
        }
        else if (status == RUNNING) {
            workingLayout();
        }
        else if (status == COMPLETED) {
            completedLayout();
        }
        else {
            wipeConstruction();
            errorLayout(status);
        }
    }

    private void workingLayout() {
        this.textView_loadingStatus.setText(R.string.loading_new_exercise_HEADS_UP);
        this.button_enterOrRetry.setVisibility(View.INVISIBLE);
    }

    private void completedLayout() {
        this.textView_loadingStatus.setText(R.string.completed_loading_of_new_exercise_HEADS_UP);
        this.button_enterOrRetry.setText(R.string.enter_exercise);
        this.button_enterOrRetry.setVisibility(View.VISIBLE);
    }

    private void errorLayout(int status) {
        this.button_enterOrRetry.setText(R.string.retry);
        this.button_enterOrRetry.setVisibility(View.VISIBLE);

        if (status == UNSPECIFIED_ERROR)
            this.textView_loadingStatus.setText(R.string.exercice_loading_unspecified_error_HEADS_UP);
        else if (status == NETWORK_ERROR)
            this.textView_loadingStatus.setText(R.string.exercice_loading_network_error_HEADS_UP);
        else if (status == TOO_FEW_GEO_OBJECTS_ERROR)
            this.textView_loadingStatus.setText(R.string.exercice_loading_too_few_geo_objects_error_HEADS_UP);
        else if (status == DEAD_SERVICE_ERROR)
            this.textView_loadingStatus.setText(R.string.exercice_loading_dead_service_error_HEADS_UP);
    }

    /**
     * - Removes session-exercise (and everything below).
     * - Removes geo-objects without a quiz (-1 -reference which new geo-objects have
     * at a point during post-processing).
     */
    private void wipeConstruction() {
        AppDatabase db = AppDatabase.getInstance(this);
        long exerciseId = SessionControl.load(db).getId();

        //todo
    }

    /**
     * Take action depending on status in session.
     * @param view
     */
    public void onEnterExerciseOrRetryConstruction(View view) {
        Session session = AppDatabase.getInstance(this).sessionDao().load();
        int status = session.getLoadingExerciseStatus();

        if (status == COMPLETED) { //enter exercise
            SessionControl.finalizeLoadingOfNewExercise(AppDatabase.getInstance(this));
            Intent intent = new Intent(this, ExerciseActivity.class);
            startActivity(intent);
        }
        else if (status < 0) { //retry construction
            killService();
            wipeConstruction();

            String name = session.getLoadingExerciseName();
            NodeShape workingArea = session.getLoadingExerciseWorkingArea();
            LoadingNewExerciseActivity.freshStart(name, workingArea, this);
        }
    }

    /**
     * User-action: exit exercise loading.
     * Clean up and close.
     * @param view
     */
    public void onExit(View view) {
        killService();
        //wait until killed
        wipeConstruction();

        SessionControl.finalizeLoadingOfNewExercise(AppDatabase.getInstance(this));
        Intent intent = new Intent(this, SelectExerciseActivity.class);
        startActivity(intent);
    }

    public void killService() {
        //todo
    }
}