package ch.windmobile.activity;

import java.util.List;

import android.content.Context;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.WindMobile.MapType;
import ch.windmobile.model.ClientFactory;
import ch.windmobile.model.StationInfo;
import ch.windmobile.view.StationOverlay;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class StationMapActivity extends MapActivity implements IStationInfosActivity {
    private static int selectionZoomLevel = 11;

    private ClientFactory clientFactory;
    private String selectedStationId;
    MapView mapView;
    StationOverlay stationOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_map);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setClickable(true);

        if (savedInstanceState != null) {
            selectedStationId = savedInstanceState.getString(IStationInfosActivity.SELECTED_STATION);
        }

        stationOverlay = new StationOverlay(this, mapView);
        mapView.getOverlays().add(stationOverlay);

        clientFactory = getWindMobile().getClientFactory();
    }

    @Override
    protected void onSaveInstanceState(Bundle outState) {
        if (getSelectedStationId() != null) {
            outState.putString(IStationInfosActivity.SELECTED_STATION, getSelectedStationId());
        }
    }

    public WindMobile getWindMobile() {
        return (WindMobile) getApplication();
    }

    public String getSelectedStationId() {
        return selectedStationId;
    }

    public void selectStation(String stationId) {
        this.selectedStationId = stationId;
        scrollToStation(selectedStationId);
    }

    @Override
    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    @Override
    protected boolean isRouteDisplayed() {
        return false;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.station_map, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh:
            selectedStationId = null;
            fitOverlays();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        MapType mapType = WindMobile.readMapType(this);
        switch (mapType) {
        case STANDARD:
            mapView.setSatellite(false);
            break;
        case SATELLITE:
            mapView.setSatellite(true);
            break;

        default:
            mapView.setSatellite(false);
        }

        if (getClientFactory().needStationInfosUpdate()) {
            getWaitForStationInfos().execute();
        } else {
            setStationInfos(clientFactory.getStationInfosCache());
        }
    }

    @Override
    protected void onPause() {
        super.onPause();
        stationOverlay.dismissPopup(false);
    }

    protected final class WaitForStationInfos extends AsyncTask<Void, Void, List<StationInfo>> {
        @Override
        protected List<StationInfo> doInBackground(Void... params) {
            try {
                return getClientFactory().getStationInfos(WindMobile.readOperationalStationOnly(StationMapActivity.this));
            } catch (Exception e) {
                return null;
            }
        }

        @Override
        protected void onPostExecute(List<StationInfo> stationInfos) {
            setStationInfos(stationInfos);
        }
    }

    @Override
    public AsyncTask<Void, Void, List<StationInfo>> getWaitForStationInfos() {
        return new WaitForStationInfos();
    }

    public void scrollToCurrentLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            int latitude = (int) (location.getLatitude() * 1E6);
            int longitude = (int) (location.getLongitude() * 1E6);
            GeoPoint point = new GeoPoint(latitude, longitude);
            mapView.getController().setZoom(selectionZoomLevel);
            mapView.getController().setCenter(point);
        }
    }

    protected void scrollToStation(String stationId) {
        StationInfo stationInfo = getClientFactory().getStationInfoCache(stationId);
        GeoPoint point = new GeoPoint(stationInfo.getLatitude(), stationInfo.getLongitude());
        mapView.getController().setZoom(selectionZoomLevel);
        mapView.getController().animateTo(point);
    }

    public void fitOverlays() {
        mapView.getController().zoomToSpan(stationOverlay.getLatSpanE6(), stationOverlay.getLonSpanE6());
        mapView.getController().animateTo(stationOverlay.getCenter());
    }

    @Override
    public void setStationInfos(List<StationInfo> stationInfos) {
        stationOverlay.setStationInfos(stationInfos);
        if (selectedStationId != null) {
            scrollToStation(selectedStationId);
        } else {
            fitOverlays();
        }
    }
}
