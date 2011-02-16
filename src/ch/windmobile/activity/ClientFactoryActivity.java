package ch.windmobile.activity;

import java.util.List;

import android.os.AsyncTask;
import android.os.Bundle;
import ch.windmobile.WindMobile;
import ch.windmobile.model.ClientFactory;
import ch.windmobile.model.StationInfo;

public abstract class ClientFactoryActivity extends WindMobileActivity implements IClientFactoryActivity {

    private ClientFactory clientFactory;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        /*
        clientFactory = (ClientFactory) getLastNonConfigurationInstance();
        */
        clientFactory = getWindMobile().getClientFactory();
    }

    /*
    @Override
    public Object onRetainNonConfigurationInstance() {
        if (getClientFactory() != null) {
            return getClientFactory();
        } else {
            Log.i("ClientFactoryActivity", "onRetainNonConfigurationInstance() --> clientFactory is null");
        }
        return null;
    }
    */

    @Override
    protected void onResume() {
        super.onResume();

        if (getClientFactory().needStationInfosUpdate()) {
            getWaitForStationInfos().execute();
        } else {
            setStationInfos(clientFactory.getStationInfosCache());
        }
    }

    @Override
    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    protected abstract class WaitForStationInfos extends AsyncTask<Void, Void, List<StationInfo>> {
        protected Exception error;

        @Override
        protected List<StationInfo> doInBackground(Void... params) {
            try {
                return getClientFactory().getStationInfos(WindMobile.readOperationalStationOnly(ClientFactoryActivity.this));
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
