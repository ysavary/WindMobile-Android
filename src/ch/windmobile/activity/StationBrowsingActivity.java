package ch.windmobile.activity;

import java.util.ArrayList;
import java.util.List;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

import android.app.ProgressDialog;
import android.content.pm.ActivityInfo;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.MotionEvent;
import android.view.Window;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.controller.CircularController;
import ch.windmobile.model.StationInfo;
import ch.windmobile.model.WindMobileException;

public class StationBrowsingActivity extends ClientFactoryActivity {
    private static final int ORIENTATION_CHANGE_DURATION = 7000;

    private ActivityDelegator activityDelegator;
    private CircularController controller;
    private AtomicBoolean shownProgressDialog = new AtomicBoolean(false);
    ProgressDialog progressDialog;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestWindowFeature(Window.FEATURE_NO_TITLE);

        controller = new CircularController();
        String currentStationId = null;
        if (savedInstanceState != null) {
            currentStationId = savedInstanceState.getString(IClientFactoryActivity.SELECTED_STATION);
        }
        if (currentStationId == null) {
            currentStationId = getIntent().getStringExtra(IClientFactoryActivity.SELECTED_STATION);
        }
        if (currentStationId != null) {
            controller.setCurrentStationId(currentStationId);
        }

        if (isLandscape()) {
            activityDelegator = new LandscapeDelegate(this, getClientFactory());
        } else {
            activityDelegator = new PortraitDelegate(this, getClientFactory());
        }
        activityDelegator.onCreate(savedInstanceState);

        progressDialog = new ProgressDialog(this);
        progressDialog.setMessage(getText(R.string.loading_text));
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (getController().getCurrentStationId() != null) {
            outState.putString(IClientFactoryActivity.SELECTED_STATION, getController().getCurrentStationId());
        } else {
            Log.i("StationBrowsingActivity", "onSaveInstanceState() --> current selection is null");
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        getActivityDelegator().onPause();
    }

    @Override
    protected void onResume() {
        super.onResume();
        getActivityDelegator().onResume();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        if (isLandscape()) {
            inflater.inflate(R.menu.station_landscape, menu);
        } else {
            inflater.inflate(R.menu.station_portrait, menu);
        }
        return true;
    }

    synchronized private void scheduleDefaultOrientation() {
        TimerTask orientationTask = getWindMobile().getOrientationTask();
        if (orientationTask != null) {
            orientationTask.cancel();
        }
        orientationTask = new TimerTask() {
            @Override
            public void run() {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_SENSOR);
                getWindMobile().setOrientationTask(null);
            }
        };
        getWindMobile().getOrientationTimer().schedule(orientationTask, ORIENTATION_CHANGE_DURATION);
        getWindMobile().setOrientationTask(orientationTask);
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh:
            getActivityDelegator().refreshView();
            return true;

        case R.id.menu_orientation:
            if (isLandscape())
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            else {
                setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_LANDSCAPE);
            }
            scheduleDefaultOrientation();
            return true;

        default:
            return super.onMenuItemSelected(featureId, item);
        }
    }

    protected void showProgressDialog() {
        if (shownProgressDialog.compareAndSet(false, true)) {
            progressDialog.show();
        }
    }

    protected void dismissProgressDialog() {
        if (shownProgressDialog.compareAndSet(true, false)) {
            try {
                progressDialog.dismiss();
            } catch (IllegalArgumentException e) {
                // Could happen if an AsyncTask is running when the screen orientation changed
            }
        }
    }

    @Override
    protected void onDestroy() {
        dismissProgressDialog();
        super.onDestroy();
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        return getActivityDelegator().onTouchEvent(event);
    }

    public CircularController getController() {
        return controller;
    }

    public ActivityDelegator getActivityDelegator() {
        return activityDelegator;
    }

    protected final class WaitForStationInfos extends ClientFactoryActivity.WaitForStationInfos {
        @Override
        protected void onPreExecute() {
            showProgressDialog();
        }

        @Override
        protected void onPostExecute(List<StationInfo> stationInfos) {
            if (stationInfos != null) {
                setStationInfos(stationInfos);
                dismissProgressDialog();
            } else {
                dismissProgressDialog();
                if (!isFinishing()) {
                    WindMobileException clientException = WindMobile.createException(StationBrowsingActivity.this, error);
                    WindMobile.buildFatalErrorDialog(StationBrowsingActivity.this, clientException).show();
                }
            }
        }
    }

    @Override
    public WaitForStationInfos getWaitForStationInfos() {
        return new WaitForStationInfos();
    }

    @Override
    public void setStationInfos(List<StationInfo> stationInfos) {
        List<String> stationIds = new ArrayList<String>(WindMobile.readFavoriteStationIds(this));
        if (stationIds.contains(controller.getCurrentStationId()) == false) {
            stationIds.add(0, controller.getCurrentStationId());
        }
        getController().setStationIds(stationIds);
        getActivityDelegator().updateView();
    }
}