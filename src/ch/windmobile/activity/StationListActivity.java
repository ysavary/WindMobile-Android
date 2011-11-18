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

import java.util.List;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.ContextMenu;
import android.view.ContextMenu.ContextMenuInfo;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.AdapterContextMenuInfo;
import android.widget.AdapterView.OnItemClickListener;
import android.widget.ListView;
import ch.windmobile.LoadingListAdapter;
import ch.windmobile.R;
import ch.windmobile.StationInfoListAdapter;
import ch.windmobile.WindMobile;
import ch.windmobile.model.StationInfo;
import ch.windmobile.model.WindMobileException;

public class StationListActivity extends StationInfosActivity implements OnItemClickListener, OnSharedPreferenceChangeListener {

    ListView listView;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        listView = (ListView) View.inflate(this, R.layout.station_list, null);
        registerForContextMenu(listView);
        setContentView(listView);

        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onDestroy() {
        PreferenceManager.getDefaultSharedPreferences(this).unregisterOnSharedPreferenceChangeListener(this);

        super.onDestroy();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.station_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_refresh:
            refreshView();
            return true;

        default:
            return super.onOptionsItemSelected(item);
        }
    };

    StationInfoListAdapter getListAdapter() {
        return (StationInfoListAdapter) listView.getAdapter();
    }

    @Override
    public void onCreateContextMenu(ContextMenu menu, View v, ContextMenuInfo menuInfo) {
        AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) menuInfo;
        StationInfo selectedStationInfo = (StationInfo) getListAdapter().getItem(adapterMenuInfo.position);

        MenuInflater inflater = getMenuInflater();
        if (selectedStationInfo.isFavorite()) {
            inflater.inflate(R.menu.station_list_context_favorite_remove, menu);
        } else {
            inflater.inflate(R.menu.station_list_context_favorite_add, menu);
        }
        menu.setHeaderTitle(selectedStationInfo.getShortName());
    }

    @Override
    public boolean onContextItemSelected(MenuItem item) {
        switch (item.getItemId()) {
        case R.id.menu_favorite_add:
            AdapterContextMenuInfo adapterMenuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
            StationInfo selectedStationInfo = (StationInfo) getListAdapter().getItem(adapterMenuInfo.position);
            selectedStationInfo.setFavorite(true);
            getListAdapter().notifyDataSetChanged();
            return true;
        case R.id.menu_favorite_remove:
            adapterMenuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
            selectedStationInfo = (StationInfo) getListAdapter().getItem(adapterMenuInfo.position);
            selectedStationInfo.setFavorite(false);
            getListAdapter().notifyDataSetChanged();
            return true;
        case R.id.menu_map:
            adapterMenuInfo = (AdapterContextMenuInfo) item.getMenuInfo();
            selectedStationInfo = (StationInfo) getListAdapter().getItem(adapterMenuInfo.position);
            StationTabActivity parentActivity = (StationTabActivity) getParent();
            parentActivity.switchToMap(selectedStationInfo.getId());
            return true;

        default:
            return super.onContextItemSelected(item);
        }
    }

    @Override
    public void onItemClick(AdapterView<?> adapterView, View v, int position, long id) {
        // Explicit intent
        Intent intent = new Intent(StationListActivity.this, StationBrowsingActivity.class);
        StationInfo selectedStationInfo = (StationInfo) getListAdapter().getItem(position);
        intent.putExtra(StationInfosActivity.SELECTED_STATION, selectedStationInfo.getId());
        startActivity(intent);
    }

    protected final class WaitForStationInfos extends StationInfosActivity.WaitForStationInfos {
        @Override
        protected void onPreExecute() {
            listView.setAdapter(new LoadingListAdapter(StationListActivity.this, R.layout.station_row_loading));
        }

        @Override
        protected void onPostExecute(List<StationInfo> stationInfos) {
            if (stationInfos != null) {
                setStationInfos(stationInfos);
            } else {
                if (!isFinishing()) {
                    WindMobileException clientException = WindMobile.createException(StationListActivity.this, error);
                    WindMobile.buildFatalErrorDialog(StationListActivity.this, clientException).show();
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
        StationInfoListAdapter adapter = new StationInfoListAdapter(this, stationInfos, R.layout.station_row, isLandscape());
        adapter.setClientFactory(getClientFactory());
        listView.setAdapter(adapter);
        listView.setOnItemClickListener(this);
    }

    public void refreshView() {
        getWaitForStationInfos().execute();
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        if ("operationalStationOnly".equals(key)) {
            getClientFactory().clear();
        }
    }
}
