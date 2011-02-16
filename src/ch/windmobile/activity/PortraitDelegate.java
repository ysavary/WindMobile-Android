package ch.windmobile.activity;

import java.util.Timer;
import java.util.TimerTask;

import android.graphics.Color;
import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.util.Log;
import android.view.GestureDetector;
import android.view.GestureDetector.SimpleOnGestureListener;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.PopupWindow;
import android.widget.TextView;
import android.widget.ViewSwitcher;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.model.ClientFactory;
import ch.windmobile.model.StationData;
import ch.windmobile.model.StationDataUtils;
import ch.windmobile.model.StationDataUtils.LastUpdate;
import ch.windmobile.model.StationInfo;
import ch.windmobile.model.WindMobileException;
import ch.windmobile.view.TrendIcon;
import ch.windmobile.view.WindDirection;

public class PortraitDelegate implements ActivityDelegator, OnClickListener {
    private static final int SWIPE_MIN_DISTANCE = 100;
    private static final int SWIPE_MAX_OFF_PATH = 300;
    private static final int SWIPE_THRESHOLD_VELOCITY = 200;

    private final StationBrowsingActivity activity;
    private final ClientFactory clientFactory;

    ViewSwitcher views;
    GestureDetector gestureDetector;
    Animation slideLeftIn;
    Animation slideLeftOut;
    Animation slideRightIn;
    Animation slideRightOut;

    final Timer timer = new Timer();
    final Handler handler = new Handler();
    TimerTask arrowTask;
    ImageButton arrowLeftButton;
    ImageButton arrowRightButton;
    PopupWindow arrowLeft;
    PopupWindow arrowRight;

    public PortraitDelegate(StationBrowsingActivity activity, ClientFactory clientFactory) {
        this.activity = activity;
        this.clientFactory = clientFactory;
    }

    @Override
    public StationBrowsingActivity getActivity() {
        return activity;
    }

    @Override
    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        views = (ViewSwitcher) View.inflate(getActivity(), R.layout.station_browsing, null);
        getActivity().setContentView(views);

        gestureDetector = new GestureDetector(getActivity(), new SwipeGestureDetector());
        slideLeftIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_left_in);
        slideLeftOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_left_out);
        slideRightIn = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_in);
        slideRightOut = AnimationUtils.loadAnimation(getActivity(), R.anim.slide_right_out);

        View arrowLeftView = LayoutInflater.from(activity).inflate(R.layout.station_arrow_left, (ViewGroup) views.getParent(), false);
        arrowLeftButton = (ImageButton) arrowLeftView.findViewById(R.id.arrow_left);
        arrowLeftButton.setOnClickListener(this);
        arrowLeftView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        arrowLeft = new PopupWindow(arrowLeftView, arrowLeftView.getMeasuredWidth(), arrowLeftView.getMeasuredHeight());

        View arrowRightView = LayoutInflater.from(activity).inflate(R.layout.station_arrow_right, (ViewGroup) views.getParent(), false);
        arrowRightButton = (ImageButton) arrowRightView.findViewById(R.id.arrow_right);
        arrowRightButton.setOnClickListener(this);
        arrowRightView.measure(View.MeasureSpec.UNSPECIFIED, View.MeasureSpec.UNSPECIFIED);
        arrowRight = new PopupWindow(arrowRightView, arrowRightView.getMeasuredWidth(), arrowRightView.getMeasuredHeight());
    }

    @Override
    public void onPause() {
        dissmissArrows(false);
    }

    @Override
    public void onResume() {
        // Don't try to update this view if we are waiting for the station's list
        if (getClientFactory().needStationInfosUpdate() == false) {
            updateView();
        }
    }

    boolean swipeEnabled() {
        return (getActivity().getController().getStationIds().size() > 1);
    }

    synchronized void showArrows() {
        if (arrowTask != null) {
            arrowTask.cancel();
        } else {
            View parent = (View) views.getParent();

            arrowLeft.setAnimationStyle(R.style.fade_out);
            arrowRight.setAnimationStyle(R.style.fade_out);

            arrowLeft.showAtLocation(parent, Gravity.NO_GRAVITY, 0, parent.getHeight() / 2);
            arrowRight.showAtLocation(parent, Gravity.NO_GRAVITY, parent.getWidth(), parent.getHeight() / 2);
        }

        arrowTask = new TimerTask() {
            @Override
            public void run() {
                handler.post(new Runnable() {
                    @Override
                    public void run() {
                        dissmissArrows(true);
                    }
                });
            }
        };
        timer.schedule(arrowTask, WindMobile.readControlsShowDelay(getActivity()));
    }

    synchronized void dissmissArrows(boolean fade) {
        if ((arrowLeft.getContentView() != null) && (arrowLeft.getContentView().getParent() != null)) {
            if (!fade) {
                arrowLeft.setAnimationStyle(0);
                arrowLeft.update();
            }
            arrowLeft.dismiss();
        }
        if ((arrowRight.getContentView() != null) && (arrowRight.getContentView().getParent() != null)) {
            if (!fade) {
                arrowRight.setAnimationStyle(0);
                arrowRight.update();
            }
            arrowRight.dismiss();
        }

        if (arrowTask != null) {
            arrowTask.cancel();
        }
        arrowTask = null;
    }

    @Override
    public boolean onTouchEvent(MotionEvent event) {
        if (swipeEnabled()) {
            showArrows();
            return gestureDetector.onTouchEvent(event);
        }
        return false;
    }

    @Override
    public void onClick(View view) {
        if (view == arrowLeftButton) {
            showPreviousStation();
            showArrows();
        } else if (view == arrowRightButton) {
            showNextStation();
            showArrows();
        }
    }

    public void showNextStation() {
        try {
            updateStation(views.getNextView(), getActivity().getController().nextStation(), false);
            views.setInAnimation(slideLeftIn);
            views.setOutAnimation(slideLeftOut);
            views.showNext();
        } catch (Exception e) {
            Log.w("PortraitDelegate", "showNextStation()", e);
        }
    }

    public void showPreviousStation() {
        try {
            updateStation(views.getNextView(), getActivity().getController().previousStation(), false);
            views.setInAnimation(slideRightIn);
            views.setOutAnimation(slideRightOut);
            views.showPrevious();
        } catch (Exception e) {
            Log.w("PortraitDelegate", "showPreviousStation()", e);
        }
    }

    final class SwipeGestureDetector extends SimpleOnGestureListener {
        @Override
        public boolean onFling(MotionEvent e1, MotionEvent e2, float velocityX, float velocityY) {
            try {
                if (Math.abs(e1.getY() - e2.getY()) > SWIPE_MAX_OFF_PATH)
                    return false;
                // right to left swipe
                if (e1.getX() - e2.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    showNextStation();
                    return true;
                } else if (e2.getX() - e1.getX() > SWIPE_MIN_DISTANCE && Math.abs(velocityX) > SWIPE_THRESHOLD_VELOCITY) {
                    showPreviousStation();
                    return true;
                }
            } catch (Exception e) {
                Log.w("PortraitDelegate.SwipeGestureDetector", "onFling()", e);
            }
            return false;
        }
    }

    @Override
    public void updateView() {
        try {
            updateStation(views.getCurrentView(), getActivity().getController().getCurrentStationId(), false);
        } catch (Exception e) {
            Log.e("PortraitDelegate", "updateView()", e);
        }
    }

    @Override
    public void refreshView() {
        try {
            updateStation(views.getCurrentView(), getActivity().getController().getCurrentStationId(), true);
        } catch (Exception e) {
            Log.e("PortraitDelegate", "refreshView()", e);
        }
    }

    void updateStation(View view, String stationId, boolean forceUpdate) {
        StationInfo stationInfo = getClientFactory().getStationInfoCache(stationId);
        StationData stationData = getClientFactory().getStationDataCache(stationId);

        if ((forceUpdate) || (getClientFactory().needStationDataUpdate(stationId, stationData))) {
            updateStationData(view, stationData);
            // Set loading text
            updateStatus(view, true, null);
            updateStationInfo(view, stationInfo);
            new WaitForStationDatas().execute(view, stationId);
        } else {
            updateStationInfo(view, stationInfo);
            updateStationData(view, stationData);
        }
    }

    final class WaitForStationDatas extends AsyncTask<Object, Void, StationData> {
        private View view;
        private String stationId;
        private Exception error;

        @Override
        protected StationData doInBackground(Object... params) {
            try {
                view = (View) params[0];
                stationId = (String) params[1];

                return getClientFactory().getStationData(stationId);
            } catch (Exception e) {
                error = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(StationData stationData) {
            if (stationData != null) {
                if ((view == views.getCurrentView()) && (stationId.equals(getActivity().getController().getCurrentStationId()))) {
                    updateStationData(view, stationData);
                }
            } else {
                WindMobileException clientException = WindMobile.createException(getActivity(), error);
                if (clientException.isFatal()) {
                    if (!getActivity().isFinishing()) {
                        WindMobile.buildFatalErrorDialog(getActivity(), clientException).show();
                    }
                } else {
                    updateStatus(view, false, clientException.getLocalizedName());
                }
            }
        }
    }

    void updateStatus(View view, boolean loading, CharSequence errorText) {
        TextView stationLastUpdate = (TextView) view.findViewById(R.id.station_lastUpdate);

        if (loading) {
            stationLastUpdate.setTextColor(Color.WHITE);
            stationLastUpdate.setText(getActivity().getText(R.string.loading_text));
        } else if (errorText != null) {
            stationLastUpdate.setTextColor(Color.RED);
            stationLastUpdate.setText(errorText);
        }
    }

    void updateStationInfo(View view, StationInfo stationInfo) {
        TextView stationName = (TextView) view.findViewById(R.id.station_name);
        TextView stationDescription = (TextView) view.findViewById(R.id.station_description);

        try {
            stationName.setText(stationInfo.getShortName());
            stationDescription.setText(getActivity().getText(R.string.altitude_label) + " " + stationInfo.getAltitude() + " "
                + getActivity().getText(R.string.meters_text));

        } catch (Exception e) {
            Log.e("PortraitDelegate", "updateStationInfo()", e);
        }
    }

    void updateStationData(View view, StationData stationData) {
        TextView stationLastUpdate = (TextView) view.findViewById(R.id.station_lastUpdate);
        TextView windAverage = (TextView) view.findViewById(R.id.wind_last_average);
        TextView windMax = (TextView) view.findViewById(R.id.wind_last_max);
        WindDirection windDirection = (WindDirection) view.findViewById(R.id.wind_direction);
        TrendIcon trendIcon = (TrendIcon) view.findViewById(R.id.wind_trend);
        TextView windHistoryMin = (TextView) view.findViewById(R.id.wind_history_min);
        TextView windHistoryAverage = (TextView) view.findViewById(R.id.wind_history_average);
        TextView windHistoryMax = (TextView) view.findViewById(R.id.wind_history_max);
        TextView airTemperature = (TextView) view.findViewById(R.id.air_temperature);
        TextView airHumidity = (TextView) view.findViewById(R.id.air_humidity);

        try {
            if (stationData != null) {
                LastUpdate lastUpdate = StationDataUtils.getRelativeLastUpdate(stationData);
                stationLastUpdate.setTextColor(lastUpdate.color);
                stationLastUpdate.setText(lastUpdate.text);

                windAverage.setText(stationData.getWindAverage());
                windMax.setText(stationData.getWindMax());

                windDirection.clearDirections();
                for (int i = 0; i < stationData.getWindDirections().length; i++) {
                    windDirection.addDirection(stationData.getWindDirections()[i]);
                }

                trendIcon.setAngle((stationData.getWindTrend()));

                windHistoryMin.setText(stationData.getWindHistoryMin());
                windHistoryAverage.setText(stationData.getWindHistoryAverage());
                windHistoryMax.setText(stationData.getWindHistoryMax());

                airTemperature.setText(stationData.getAirTemperature());
                airHumidity.setText(stationData.getAirHumidity());
            } else {
                // Clear the form
                stationLastUpdate.setText(null);
                windAverage.setText(null);
                windMax.setText(null);
                windDirection.clearDirections();
                trendIcon.setAngle(-1);
                windHistoryMin.setText(null);
                windHistoryAverage.setText(null);
                windHistoryMax.setText(null);
                airTemperature.setText(null);
                airHumidity.setText(null);

            }
        } catch (Exception e) {
            Log.e("PortraitDelegate", "updateStationData()", e);
        }
    }
}
