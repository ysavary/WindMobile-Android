package ch.windmobile.activity;

import android.os.Bundle;
import android.preference.PreferenceActivity;
import ch.windmobile.R;

public class PreferencesActivity extends PreferenceActivity {
    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }
}
