package ch.windmobile.activity;

import java.util.List;

import android.os.AsyncTask;
import ch.windmobile.WindMobile;
import ch.windmobile.model.StationInfo;

public abstract class StationInfosActivity extends ClientFactoryActivity implements IStationInfosActivity {

    @Override
    protected void onResume() {
        super.onResume();

        if (getClientFactory().needStationInfosUpdate()) {
            getWaitForStationInfos().execute();
        } else {
            setStationInfos(getClientFactory().getStationInfosCache());
        }
    }

    protected abstract class WaitForStationInfos extends AsyncTask<Void, Void, List<StationInfo>> {
        protected Exception error;

        @Override
        protected List<StationInfo> doInBackground(Void... params) {
            try {
                return getClientFactory().getStationInfos(WindMobile.readOperationalStationOnly(StationInfosActivity.this));
            } catch (Exception e) {
                error = e;
                return null;
            }
        }
    }

    @Override
    public abstract WaitForStationInfos getWaitForStationInfos();

    @Override
    public abstract void setStationInfos(List<StationInfo> stationInfos);
}
