package ch.windmobile.activity;

import java.util.HashSet;
import java.util.List;
import java.util.Set;

import android.appwidget.AppWidgetManager;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import ch.windmobile.LoadingListAdapter;
import ch.windmobile.R;
import ch.windmobile.StationInfoListAdapter;
import ch.windmobile.WidgetProvider;
import ch.windmobile.WindMobile;
import ch.windmobile.model.StationInfo;
import ch.windmobile.model.WindMobileException;

public class WidgetConfigurationActivity extends ClientFactoryActivity implements OnItemClickListener {

    public static final String PREFS_NAME = "ch.windmobile.activity.WidgetConfigurationActivity";
    public static final String PREF_STATION_ID_KEY = "station_id_";
    public static final String PREF_STATION_NAME_KEY = "station_name_";

    ListView listView;
    int widgetId = AppWidgetManager.INVALID_APPWIDGET_ID;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listView = (ListView) View.inflate(this, R.layout.station_list, null);
        setContentView(listView);

        Intent intent = getIntent();
        Bundle extras = intent.getExtras();
        if (extras != null) {
            widgetId = extras.getInt(AppWidgetManager.EXTRA_APPWIDGET_ID, AppWidgetManager.INVALID_APPWIDGET_ID);
        }

        if (widgetId == AppWidgetManager.INVALID_APPWIDGET_ID) {
            finish();
        }
    }

    public StationInfoListAdapter getListAdapter() {
        return (StationInfoListAdapter) listView.getAdapter();
    }

    public Set<String> getFavoriteIds() {
        try {
            Set<String> favoriteIds = new HashSet<String>();
            for (StationInfo stationInfo : getListAdapter().getStationInfos()) {
                if (stationInfo.isFavorite()) {
                    favoriteIds.add(stationInfo.getId());
                }
            }
            return favoriteIds;
        } catch (Exception e) {
            return null;
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        StationInfo stationInfo = (StationInfo) getListAdapter().getItem(position);

        saveWidgetStationInfo(this, widgetId, stationInfo);
        WidgetProvider.updateWidgetInService(this, widgetId, stationInfo.getId());

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    protected final class WaitForStationInfos extends ClientFactoryActivity.WaitForStationInfos {
        @Override
        protected void onPreExecute() {
            listView.setAdapter(new LoadingListAdapter(WidgetConfigurationActivity.this, R.layout.station_row_loading));
        }

        @Override
        protected void onPostExecute(List<StationInfo> stationInfos) {
            if (stationInfos != null) {
                setStationInfos(stationInfos);
            } else {
                if (!isFinishing()) {
                    WindMobileException clientException = WindMobile.createException(WidgetConfigurationActivity.this, error);
                    WindMobile.buildFatalErrorDialog(WidgetConfigurationActivity.this, clientException).show();
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
        listView.setAdapter(new StationInfoListAdapter(this, stationInfos, R.layout.widget_station_row, isLandscape()));
        listView.setOnItemClickListener(this);
    }

    public void refreshView() {
        getWaitForStationInfos().execute();
    }

    public static void saveWidgetStationInfo(Context context, int widgetId, StationInfo stationInfo) {
        SharedPreferences.Editor prefsEditor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        prefsEditor.putString(PREF_STATION_ID_KEY + widgetId, stationInfo.getId());
        prefsEditor.putString(PREF_STATION_NAME_KEY + widgetId, stationInfo.getShortName());
        prefsEditor.commit();
    }

    public static void deleteWidgetId(Context context, int widgetId) {
        SharedPreferences.Editor prefsEditor = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE).edit();
        prefsEditor.remove(PREF_STATION_ID_KEY + widgetId);
        prefsEditor.remove(PREF_STATION_NAME_KEY + widgetId);
        prefsEditor.commit();
    }

    public static String loadWidgetStationId(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String stationId = prefs.getString(PREF_STATION_ID_KEY + widgetId, null);
        if (stationId != null) {
            return stationId;
        } else {
            throw new RuntimeException("Unable to get stationId for widget id '" + widgetId + "'");
        }
    }

    public static String loadWidgetStationName(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, Context.MODE_PRIVATE);
        String stationName = prefs.getString(PREF_STATION_NAME_KEY + widgetId, null);
        if (stationName != null) {
            return stationName;
        } else {
            throw new RuntimeException("Unable to get stationName for widget id '" + widgetId + "'");
        }
    }
}
