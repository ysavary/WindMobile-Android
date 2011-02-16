package ch.windmobile.activity;

import java.util.List;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.location.Location;
import android.location.LocationManager;
import android.os.AsyncTask;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.model.ClientFactory;
import ch.windmobile.model.StationInfo;
import ch.windmobile.view.StationOverlay;

import com.google.android.maps.GeoPoint;
import com.google.android.maps.MapActivity;
import com.google.android.maps.MapView;

public class StationMapActivity extends MapActivity implements IClientFactoryActivity {

    private ClientFactory clientFactory;
    MapView mapView;
    StationOverlay stationOverlay;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_map);

        mapView = (MapView) findViewById(R.id.mapview);
        mapView.setBuiltInZoomControls(true);
        mapView.setClickable(true);

        stationOverlay = new StationOverlay(this, mapView);
        mapView.getOverlays().add(stationOverlay);

        clientFactory = getWindMobile().getClientFactory();
    }

    public WindMobile getWindMobile() {
        return (WindMobile) getApplication();
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
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {

        case R.id.menu_preferences:
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;

        case R.id.menu_about:
            showDialog(WindMobile.ABOUT_DIALOG_ID);
            return true;

        default:
            return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

        case WindMobile.ABOUT_DIALOG_ID:
            Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.menu_about_title);
            builder.setIcon(R.drawable.windmobile);

            View view = View.inflate(this, R.layout.about, null);
            TextView messageTextView = (TextView) view.findViewById(R.id.about_message);
            messageTextView.setText(WindMobile.getName(this) + " " + WindMobile.getVersion(this));
            builder.setView(view);

            builder.setPositiveButton("Ok", null);
            return builder.create();

        default:
            return super.onCreateDialog(id);
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        mapView.setSatellite(WindMobile.readMapSatellite(this));

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

    public void setDefaultLocation() {
        LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
        if (location != null) {
            int latitude = (int) (location.getLatitude() * 1E6);
            int longitude = (int) (location.getLongitude() * 1E6);
            GeoPoint point = new GeoPoint(latitude, longitude);
            mapView.getController().setZoom(10);
            mapView.getController().setCenter(point);
        }
    }

    public void scrollToStation(String stationId) {
        StationInfo stationInfo = getClientFactory().getStationInfoCache(stationId);
        GeoPoint point = new GeoPoint(stationInfo.getLatitude(), stationInfo.getLongitude());
        mapView.getController().setZoom(10);
        mapView.getController().animateTo(point);
    }

    @Override
    public void setStationInfos(List<StationInfo> stationInfos) {
        stationOverlay.setStationInfos(stationInfos);
    }
}
