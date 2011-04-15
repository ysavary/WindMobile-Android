package ch.windmobile.activity;

import android.app.Activity;
import android.content.res.Configuration;
import android.os.Bundle;
import ch.windmobile.WindMobile;

public abstract class WindMobileActivity extends Activity {
    private boolean isLandscape;

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        isLandscape = (getResources().getConfiguration().orientation == Configuration.ORIENTATION_LANDSCAPE);
    }

    public boolean isLandscape() {
        return isLandscape;
    }

    public WindMobile getWindMobile() {
        return (WindMobile) getApplication();
    }
}
