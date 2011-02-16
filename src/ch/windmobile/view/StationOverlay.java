package ch.windmobile.view;

import java.util.List;

import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.os.AsyncTask;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.activity.IClientFactoryActivity;
import ch.windmobile.activity.StationBrowsingActivity;
import ch.windmobile.activity.StationMapActivity;
import ch.windmobile.model.StationData;
import ch.windmobile.model.StationDataUtils;
import ch.windmobile.model.StationInfo;
import ch.windmobile.model.WindMobileException;
import ch.windmobile.model.StationDataUtils.LastUpdate;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.ItemizedOverlay;
import com.google.android.maps.MapView;
import com.google.android.maps.OverlayItem;

public class StationOverlay extends ItemizedOverlay<OverlayItem> implements OnClickListener {

    final StationMapActivity activity;
    final MapView mapView;
    List<StationInfo> stationInfos;

    final View popupView;
    final PopupWindow popupWindow;
    final String[] directionLabels;

    String currentStationId;

    static Drawable boundIcon(Drawable icon) {
        int width = icon.getMinimumWidth();
        int height = icon.getMinimumHeight();
        int translation = (int) (width * 6d / height);
        icon.setBounds(-translation, -height + 1, width - translation, 1);
        return icon;
    }

    public StationOverlay(StationMapActivity activity, MapView mapView) {
        super(boundIcon(activity.getResources().getDrawable(R.drawable.windmobile)));

        this.activity = activity;
        this.mapView = mapView;

        popupView = LayoutInflater.from(activity).inflate(R.layout.station_map_popup, (ViewGroup) mapView.getParent(), false);
        popupView.setOnClickListener(this);
        popupWindow = new PopupWindow(popupView, (int) getActivity().getWindMobile().toPixel(180), (int) getActivity().getWindMobile().toPixel(105));

        directionLabels = activity.getResources().getStringArray(R.array.directions);
    }

    StationMapActivity getActivity() {
        return activity;
    }

    public List<StationInfo> getStationInfos() {
        return stationInfos;
    }

    public void setStationInfos(List<StationInfo> stationInfos) {
        this.stationInfos = stationInfos;
        populate();
    }

    @Override
    protected OverlayItem createItem(int i) {
        StationInfo stationInfo = getStationInfos().get(i);
        GeoPoint point = new GeoPoint(stationInfo.getLatitude(), stationInfo.getLongitude());
        return new OverlayItem(point, stationInfo.getName(), stationInfo.getName());
    }

    @Override
    public int size() {
        if (getStationInfos() == null) {
            return 0;
        }
        return getStationInfos().size();
    }

    public void dismissPopup(boolean fade) {
        if (!fade) {
            popupWindow.setAnimationStyle(0);
            popupWindow.update();
        }
        currentStationId = null;
        popupWindow.dismiss();
    }

    @Override
    protected boolean onTap(int index) {
        OverlayItem item = getItem(index);
        GeoPoint geo = item.getPoint();
        Point pt = mapView.getProjection().toPixels(geo, null);

        StationInfo stationInfo = getStationInfos().get(index);
        currentStationId = stationInfo.getId();

        TextView stationName = (TextView) popupView.findViewById(R.id.station_name);
        stationName.setText(stationInfo.getShortName());

        StationData stationData = activity.getClientFactory().getStationDataCache(currentStationId);
        if (activity.getClientFactory().needStationDataUpdate(currentStationId, stationData)) {
            updateStationData(popupView, null);
            updateStatus(popupView, true, null);
            new WaitForStationDatas().execute(popupView, currentStationId);
        } else {
            updateStationData(popupView, stationData);
        }
        popupWindow.setAnimationStyle(R.style.fade_inout);
        popupWindow.showAtLocation((ViewGroup) mapView.getParent(), Gravity.NO_GRAVITY, pt.x, pt.y);

        return true;
    }

    @Override
    public boolean onTap(GeoPoint p, MapView mapView) {
        dismissPopup(true);
        return super.onTap(p, mapView);
    }

    @Override
    public boolean onTouchEvent(MotionEvent event, MapView mapView) {
        if (event.getAction() == MotionEvent.ACTION_MOVE) {
            dismissPopup(true);
        }
        return super.onTouchEvent(event, mapView);
    }

    @Override
    public void onClick(View v) {
        // Explicit intent
        Intent intent = new Intent(getActivity(), StationBrowsingActivity.class);
        intent.putExtra(IClientFactoryActivity.SELECTED_STATION, currentStationId);
        getActivity().startActivity(intent);

        dismissPopup(true);
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

                return activity.getClientFactory().getStationData(stationId);
            } catch (Exception e) {
                error = e;
                return null;
            }
        }

        @Override
        protected void onPostExecute(StationData stationData) {
            if (stationData != null) {
                updateStationData(view, stationData);
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

    void updateStationData(View view, StationData stationData) {
        TextView stationLastUpdate = (TextView) view.findViewById(R.id.station_lastUpdate);
        TextView windDirection = (TextView) view.findViewById(R.id.wind_direction);
        TextView windAverage = (TextView) view.findViewById(R.id.wind_last_average);
        TextView windMax = (TextView) view.findViewById(R.id.wind_last_max);
        TrendIcon trendIcon = (TrendIcon) view.findViewById(R.id.wind_trend);

        try {
            if (stationData != null) {
                LastUpdate lastUpdate = StationDataUtils.getRelativeLastUpdate(stationData);
                stationLastUpdate.setTextColor(lastUpdate.color);
                stationLastUpdate.setText(lastUpdate.text);
                try {
                    float[] directions = stationData.getWindDirections();
                    // Takes the last value
                    float direction = directions[directions.length - 1];
                    windDirection.setText(StationDataUtils.getWindDirectionLabel(directionLabels, direction));
                } catch (Exception e) {
                    Log.e("StationOverlay", "Unable to get wind direction label", e);
                    windDirection.setText("ER");
                }
                windAverage.setText(stationData.getWindAverage());
                windMax.setText(stationData.getWindMax());
                trendIcon.setAngle(stationData.getWindTrend());

            } else {
                // Clear the form
                stationLastUpdate.setText(null);
                windDirection.setText(null);
                windAverage.setText(null);
                windMax.setText(null);
                trendIcon.setAngle(-1);
            }
        } catch (Exception e) {
            Log.e("StationOverlay", "updateStationData()", e);
        }
    }
}
