package ch.windmobile.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import ch.windmobile.WindMobile;
import ch.windmobile.model.ClientFactory;

public abstract class ClientFactoryActivity extends Activity implements IClientFactoryActivity {
    private ClientFactory clientFactory;
    private boolean isLandscape;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        clientFactory = getWindMobile().getClientFactory();
        isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public WindMobile getWindMobile() {
        return (WindMobile) getApplication();
    }
}
