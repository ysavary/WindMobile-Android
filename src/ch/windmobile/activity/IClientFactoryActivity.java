package ch.windmobile.activity;

import java.util.List;

import android.os.AsyncTask;
import ch.windmobile.model.ClientFactory;
import ch.windmobile.model.StationInfo;

public interface IClientFactoryActivity {
    public static final String SELECTED_STATION = "ch.windmobile.selectedStation";

    ClientFactory getClientFactory();

    AsyncTask<Void, Void, List<StationInfo>> getWaitForStationInfos();

    void setStationInfos(List<StationInfo> stationInfos);
}
