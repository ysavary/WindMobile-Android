package ch.windmobile.activity;

import java.util.ArrayList;
import java.util.List;

import android.app.TabActivity;
import android.content.Intent;
import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.graphics.drawable.LayerDrawable;
import android.os.Bundle;
import android.widget.TabHost;
import ch.windmobile.R;
import ch.windmobile.WindMobile;
import ch.windmobile.model.ClientFactory;
import ch.windmobile.view.BadgeHelper;

public class StationTabActivity extends TabActivity implements IClientFactoryActivity {
    static final String stationListActivityId = "stationListActivity";
    static final String mapActivityId = "mapActivity";
    static final String chatListActivityId = "socialActivity";
    static final String preferencesActivityId = "preferencesActivity";

    private ClientFactory clientFactory;
    TabHost tabHost;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clientFactory = ((WindMobile) getApplication()).getClientFactory();
        setContentView(R.layout.station_tabs);

        Resources res = getResources();
        tabHost = getTabHost();
        TabHost.TabSpec spec;
        Intent intent;

        intent = new Intent(this, StationListActivity.class);
        spec = tabHost.newTabSpec(stationListActivityId).setIndicator(res.getText(R.string.tab_station_list), res.getDrawable(R.drawable.tab_list))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent(this, StationMapActivity.class);
        spec = tabHost.newTabSpec(mapActivityId).setIndicator(res.getText(R.string.tab_station_map), res.getDrawable(R.drawable.tab_map))
            .setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent(this, ChatListActivity.class);
        Drawable tab_chat = res.getDrawable(R.drawable.tab_chat);
        try {
            List<Long> messagesIds = getClientFactory().getLastMessageIds(new ArrayList<String>(getClientFactory().getFavorites()));
            int total = 0;
            for (Long count : messagesIds) {
                if (count > 0) {
                    total += count;
                }
            }
            Drawable badge = BadgeHelper.drawBadge(this, total, tab_chat.getIntrinsicWidth(), tab_chat.getIntrinsicHeight(), 55);
            tab_chat = new LayerDrawable(new Drawable[] { tab_chat, badge });
        } catch (Exception e) {
        }

        spec = tabHost.newTabSpec(chatListActivityId).setIndicator(res.getText(R.string.tab_chat), tab_chat).setContent(intent);
        tabHost.addTab(spec);

        intent = new Intent(this, PreferencesActivity.class);
        spec = tabHost.newTabSpec(preferencesActivityId).setIndicator(res.getText(R.string.tab_settings), res.getDrawable(R.drawable.tab_settings))
            .setContent(intent);
        tabHost.addTab(spec);

        tabHost.setCurrentTab(0);
    }

    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    public void switchToMap(String stationId) {
        tabHost.setCurrentTab(1);
        StationMapActivity mapActivity = (StationMapActivity) getLocalActivityManager().getActivity(mapActivityId);
        mapActivity.selectStation(stationId);
    }
}
