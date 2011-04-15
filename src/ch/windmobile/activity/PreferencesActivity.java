package ch.windmobile.activity;

import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.os.Bundle;
import android.preference.PreferenceActivity;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import ch.windmobile.R;
import ch.windmobile.WindMobile;

public class PreferencesActivity extends PreferenceActivity {

    @Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.station_settings, menu);
        return true;
    }

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {

        case R.id.menu_about:
            showDialog(WindMobile.ABOUT_DIALOG_ID);
            return true;

        default:
            return super.onMenuItemSelected(featureId, item);
        }
    }

    @Override
    protected Dialog onCreateDialog(int id) {
        switch (id) {

        case WindMobile.ABOUT_DIALOG_ID:
            Builder builder = new AlertDialog.Builder(this);
            builder.setTitle(R.string.menu_about_title);
            builder.setIcon(R.drawable.windmobile);

            View view = View.inflate(this, R.layout.about, null);
            TextView messageTextView = (TextView) view.findViewById(R.id.about_message);
            messageTextView.setText(WindMobile.getName(this) + " " + WindMobile.getVersion(this));
            builder.setView(view);

            builder.setPositiveButton("Ok", null);
            return builder.create();

        default:
            return super.onCreateDialog(id);
        }
    }
}
