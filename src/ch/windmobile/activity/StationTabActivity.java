package ch.windmobile.activity;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.os.Bundle;
import android.widget.TabHost;
import ch.windmobile.R;

public class StationTabActivity extends TabActivity {
    static final String listActivityId = "listActivity";
    static final String mapActivityId = "mapActivity";
    static final String socialActivityId = "socialActivity";
    static final String preferencesActivityId = "preferencesActivity";

    TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.station_tabs);

        Resources res = getResources();
        tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;

        intent = new Intent(this, StationListActivity.class);
        spec = tabHost.newTabSpec(listActivityId).setIndicator(res.getText(R.string.tab_station_list), res.getDrawable(R.drawable.tab_list))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent(this, StationMapActivity.class);
        spec = tabHost.newTabSpec(mapActivityId).setIndicator(res.getText(R.string.tab_station_map), res.getDrawable(R.drawable.tab_map))
            .setContent(intent);
        tabHost.addTab(spec);
        
        intent = new Intent(this, SocialActivity.class);
        spec = tabHost.newTabSpec(socialActivityId).setIndicator(res.getText(R.string.tab_station_map), res.getDrawable(R.drawable.tab_map))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent(this, PreferencesActivity.class);
        spec = tabHost.newTabSpec(preferencesActivityId).setIndicator(res.getText(R.string.tab_settings), res.getDrawable(R.drawable.tab_settings))
            .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }

    public void switchToMap(String stationId) {
        tabHost.setCurrentTab(1);
        StationMapActivity mapActivity = (StationMapActivity) getLocalActivityManager().getActivity(mapActivityId);
        mapActivity.selectStation(stationId);
    }
}
