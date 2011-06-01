package ch.windmobile.activity;

import java.util.List;

import android.os.AsyncTask;
import ch.windmobile.model.StationInfo;

public interface IStationInfosActivity extends IClientFactoryActivity {
    public static final String SELECTED_STATION = "ch.windmobile.selectedStation";

    AsyncTask<Void, Void, List<StationInfo>> getWaitForStationInfos();

    void setStationInfos(List<StationInfo> stationInfos);
}
