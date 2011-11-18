/*******************************************************************************
 * Copyright (c) 2011 epyx SA.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
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

public class WidgetConfigurationActivity extends StationInfosActivity implements OnItemClickListener {

    private static final String PREFS_NAME = "ch.windmobile.activity.WidgetConfigurationActivity";
    private static final String PREF_STATION_ID_KEY = "station_id_";
    private static final String PREF_STATION_NAME_KEY = "station_name_";

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

    StationInfoListAdapter getListAdapter() {
        return (StationInfoListAdapter) listView.getAdapter();
    }

    Set<String> getFavoriteIds() {
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
        StationInfo selectedStationInfo = (StationInfo) getListAdapter().getItem(position);

        saveWidgetStationInfo(this, widgetId, selectedStationInfo);
        WidgetProvider.updateWidget(this, widgetId);

        Intent resultValue = new Intent();
        resultValue.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, widgetId);
        setResult(RESULT_OK, resultValue);
        finish();
    }

    protected final class WaitForStationInfos extends StationInfosActivity.WaitForStationInfos {
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

    private static String getStationIdKey(int widgetId) {
        return PREF_STATION_ID_KEY + widgetId;
    }

    private static String getStationNameKey(int widgetId) {
        return PREF_STATION_NAME_KEY + widgetId;
    }

    public static void saveWidgetStationInfo(Context context, int widgetId, StationInfo stationInfo) {
        SharedPreferences.Editor prefsEditor = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefsEditor.putString(getStationIdKey(widgetId), stationInfo.getId());
        prefsEditor.putString(getStationNameKey(widgetId), stationInfo.getName());
        prefsEditor.commit();
    }

    public static void deleteWidgetId(Context context, int widgetId) {
        SharedPreferences.Editor prefsEditor = context.getSharedPreferences(PREFS_NAME, 0).edit();
        prefsEditor.remove(getStationIdKey(widgetId));
        prefsEditor.remove(getStationNameKey(widgetId));
    }

    public static String loadWidgetStationId(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String key = getStationIdKey(widgetId);
        String stationId = prefs.getString(key, null);
        if (stationId != null) {
            return stationId;
        } else {
            throw new RuntimeException("Unable to get stationId for widget id '" + widgetId + "'");
        }
    }

    public static String loadWidgetStationName(Context context, int widgetId) {
        SharedPreferences prefs = context.getSharedPreferences(PREFS_NAME, 0);
        String key = getStationNameKey(widgetId);
        String stationId = prefs.getString(key, null);
        if (stationId != null) {
            return stationId;
        } else {
            throw new RuntimeException("Unable to get stationName for widget id '" + widgetId + "'");
        }
    }
}
