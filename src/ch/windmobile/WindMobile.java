package ch.windmobile;

import java.io.IOException;
import java.io.InterruptedIOException;
import java.net.SocketException;
import java.text.ParseException;
import java.util.HashSet;
import java.util.Locale;
import java.util.Set;
import java.util.Timer;
import java.util.TimerTask;

import org.json.JSONException;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Application;
import android.app.Dialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.SharedPreferences.Editor;
import android.content.pm.PackageInfo;
import android.content.pm.PackageManager.NameNotFoundException;
import android.content.res.Configuration;
import android.os.Build;
import android.preference.PreferenceManager;
import android.util.Log;
import ch.windmobile.model.ClientException;
import ch.windmobile.model.ClientFactory;
import ch.windmobile.model.WindMobileException;

public class WindMobile extends Application {
    public static final int ABOUT_DIALOG_ID = 1;

    private static float density;
    private static String version;
    private static String userAgent;

    private ClientFactory clientFactory;
    private final Timer orientationTimer = new Timer();
    private TimerTask orientationTask;

    @Override
    public void onCreate() {
        String[] serverUrls = getResources().getStringArray(R.array.servers);
        clientFactory = new ClientFactory(this, serverUrls);
    }

    @Override
    public void onLowMemory() {
        clientFactory.clear();
    }

    @Override
    public void onConfigurationChanged(Configuration newConfig) {
        density = 0;
    }

    public ClientFactory getClientFactory() {
        return clientFactory;
    }

    public Timer getOrientationTimer() {
        return orientationTimer;
    }

    public TimerTask getOrientationTask() {
        return orientationTask;
    }

    public void setOrientationTask(TimerTask orientationTask) {
        this.orientationTask = orientationTask;
    }

    public float getDensity() {
        if (density == 0) {
            density = getResources().getDisplayMetrics().density;
        }
        return density;
    }

    public static float toPixel(int dp, float density) {
        return dp * density + 0.5f;
    }

    public float toPixel(int dp) {
        return toPixel(dp, getDensity());
    }

    public static String getName(Context context) {
        return context.getString(R.string.app_name);
    }

    public static String getVersion(Context context) {
        if (version == null) {
            try {
                PackageInfo packageInfo = context.getPackageManager().getPackageInfo("ch.windmobile", 0);
                version = packageInfo.versionName;
            } catch (NameNotFoundException e) {
                version = "Unknown";
            }
        }
        return version;
    }

    public static String getUserAgent(Context context) {
        if (userAgent == null) {
            userAgent = getName(context) + "/" + getVersion(context) + " Android/" + Build.VERSION.SDK_INT + " (" + Locale.getDefault() + ") "
                + Build.MANUFACTURER + "/" + Build.MODEL;
        }
        return userAgent;
    }

    public static boolean readOperationalStationOnly(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("operationalStationOnly", true);
    }

    public static boolean readMapSatellite(Context context) {
        return PreferenceManager.getDefaultSharedPreferences(context).getBoolean("mapSatellite", true);
    }

    public static int readChartDuration(Context context) {
        String value = PreferenceManager.getDefaultSharedPreferences(context).getString("chartDuration", "14400");
        return Integer.decode(value);
    }

    public static Set<String> readFavoriteStationIds(Context context) {
        String value = PreferenceManager.getDefaultSharedPreferences(context).getString("favoriteStationIds", "");
        String[] ids = value.split(",");

        Set<String> set = new HashSet<String>(ids.length);
        for (String id : ids) {
            if (id.equals("") == false) {
                set.add(id);
            }
        }
        return set;
    }

    public static void writeFavoriteStationIds(Context context, Set<String> ids) {
        StringBuilder builder = new StringBuilder();
        for (String id : ids) {
            if (builder.length() != 0) {
                builder.append(",");
            }
            builder.append(id);
        }
        Editor prefsEditor = PreferenceManager.getDefaultSharedPreferences(context).edit();
        prefsEditor.putString("favoriteStationIds", builder.toString());
        prefsEditor.commit();
    }

    public static int readControlsShowDelay(Context context) {
        String value = PreferenceManager.getDefaultSharedPreferences(context).getString("controlsShowDelay", "2000");
        return Integer.decode(value);
    }

    public static WindMobileException createException(Context context, Throwable e) {
        Log.w("WindMobile", "createException()", e);

        if (e instanceof WindMobileException) {
            return (WindMobileException) e;
        } else if (e instanceof JSONException) {
            String message = e.getClass().getSimpleName() + ": " + e.getLocalizedMessage();
            return new ClientException(context.getText(R.string.data_error), message);
        } else if (e instanceof ParseException) {
            String message = e.getClass().getSimpleName() + ": " + e.getLocalizedMessage();
            return new ClientException(context.getText(R.string.data_error), message);
        } else if ((e instanceof SocketException) || (e instanceof InterruptedIOException)) {
            String message = e.getClass().getSimpleName() + ": " + e.getLocalizedMessage();
            return new ClientException(context.getText(R.string.network_error), message);
        } else if (e instanceof IOException) {
            String message = e.getClass().getSimpleName() + ": " + e.getLocalizedMessage();
            return new ClientException(context.getText(R.string.network_error), message, true);
        } else {
            String message = e.getClass().getSimpleName() + ": " + e.getLocalizedMessage();
            return new ClientException(context.getText(R.string.unknown_error), message);
        }
    }

    public static Dialog buildErrorDialog(final Activity activity, WindMobileException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(e.getLocalizedName());
        builder.setMessage(e.getLocalizedMessage());
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.button_ok, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                dialog.dismiss();
            }
        });
        builder.setNegativeButton(R.string.button_quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                activity.finish();
            }
        });
        return builder.create();
    }

    public static Dialog buildFatalErrorDialog(final Activity activity, WindMobileException e) {
        AlertDialog.Builder builder = new AlertDialog.Builder(activity);
        builder.setTitle(e.getLocalizedName());
        builder.setMessage(e.getLocalizedMessage());
        builder.setCancelable(false);
        builder.setPositiveButton(R.string.button_quit, new DialogInterface.OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int id) {
                activity.finish();
            }
        });
        return builder.create();
    }
}
