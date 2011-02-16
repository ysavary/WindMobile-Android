package ch.windmobile.activity;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.AlertDialog.Builder;
import android.app.Dialog;
import android.content.Intent;
import android.content.res.Configuration;
import android.os.Bundle;
import android.view.MenuItem;
import android.view.View;
import android.widget.TextView;
import ch.windmobile.R;
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

    @Override
    public boolean onMenuItemSelected(int featureId, MenuItem item) {
        switch (item.getItemId()) {

        case R.id.menu_preferences:
            startActivity(new Intent(this, PreferencesActivity.class));
            return true;

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
