package ch.windmobile.activity;

import java.util.ArrayList;
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

public class ChatListActivity extends StationInfosActivity implements OnItemClickListener, OnSharedPreferenceChangeListener {

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
        Intent intent = new Intent(ChatListActivity.this, ChatActivity.class);
        StationInfo selectedStationInfo = (StationInfo) getListAdapter().getItem(position);
        intent.putExtra(StationInfosActivity.SELECTED_STATION, selectedStationInfo.getId());
        startActivity(intent);
    }

    protected final class WaitForStationInfos extends StationInfosActivity.WaitForStationInfos {
        @Override
        protected void onPreExecute() {
            listView.setAdapter(new LoadingListAdapter(ChatListActivity.this, R.layout.station_row_loading));
        }

        @Override
        protected void onPostExecute(List<StationInfo> stationInfos) {
            if (stationInfos != null) {
                setStationInfos(stationInfos);
            } else {
                if (!isFinishing()) {
                    WindMobileException clientException = WindMobile.createException(ChatListActivity.this, error);
                    WindMobile.buildFatalErrorDialog(ChatListActivity.this, clientException).show();
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
        // Display only favorites stations
        List<StationInfo> favoritesStations = new ArrayList<StationInfo>();
        for (StationInfo stationInfo : stationInfos) {
            if (stationInfo.isFavorite()) {
                favoritesStations.add(stationInfo);
            }
        }

        StationInfoListAdapter adapter = new StationInfoListAdapter(this, favoritesStations, R.layout.chat_station_row, isLandscape());
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
