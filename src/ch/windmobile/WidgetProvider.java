package ch.windmobile;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.support.v4.app.TaskStackBuilder;
import android.util.Log;
import android.widget.RemoteViews;
import ch.windmobile.activity.IClientFactoryActivity;
import ch.windmobile.activity.StationBrowsingActivity;
import ch.windmobile.activity.WidgetConfigurationActivity;
import ch.windmobile.model.ClientFactory;
import ch.windmobile.model.StationData;
import ch.windmobile.model.StationDataUtils;
import ch.windmobile.model.StationDataUtils.LastUpdate;

public class WidgetProvider extends AppWidgetProvider {
    private static final String WIDGET_ID = "ch.windmobile.widgetId";

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY = 20 * 1000;

    static Pattern stationIdPattern = Pattern.compile("(.+?):(.+?)-(.+?)");
    static Pattern legagyStationIdPattern = Pattern.compile("(.+?):([0-9]+)");

    protected static String migrateLegacyStationId(String stationId) {
        Matcher legacyMatcher = legagyStationIdPattern.matcher(stationId);
        if (legacyMatcher.matches()) {
            try {
                String provider = legacyMatcher.group(1);
                String id = legacyMatcher.group(2);
                return provider + ":" + provider + "-" + id;
            } catch (Exception e) {
            }
        }
        return null;
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager widgetManager, int[] widgetIds) {
        Log.i("WidgetProvider", "onUpdate() : " + widgetIds.length + " widget ids");

        for (int widgetId : widgetIds) {
            String stationId;
            try {
                stationId = WidgetConfigurationActivity.loadWidgetStationId(context, widgetId);
                if (!stationIdPattern.matcher(stationId).matches()) {
                    stationId = migrateLegacyStationId(stationId);
                    if (stationId != null) {
                        SharedPreferences.Editor prefsEditor = context.getSharedPreferences(WidgetConfigurationActivity.PREFS_NAME,
                            Context.MODE_PRIVATE).edit();
                        prefsEditor.putString(WidgetConfigurationActivity.PREF_STATION_ID_KEY + widgetId, stationId);
                        prefsEditor.commit();
                    }
                }
            } catch (Exception e) {
                stationId = null;
            }

            try {
                if (stationId != null) {
                    updateWidgetInService(context, widgetId, stationId);
                } else {
                    updateWidget(context, widgetId, null, null, null, context.getResources().getString(R.string.widget_error));
                }
            } catch (Exception e) {
                Log.w("WidgetProvider", "onUpdate()", e);
            }
        }
    }

    @Override
    public void onDeleted(Context context, int[] widgetIds) {
        for (int widgetId : widgetIds) {
            Log.i("WidgetProvider", "onDelete() : widget id '" + widgetId + "'");
            WidgetConfigurationActivity.deleteWidgetId(context, widgetId);
        }
    }

    public static void updateWidgetInService(Context context, int widgetId, String stationId) {
        Log.i("WidgetProvider", "updateAppWidget() : widgetId '" + widgetId + "'");

        String stationName = WidgetConfigurationActivity.loadWidgetStationName(context, widgetId);
        WidgetProvider.updateWidget(context, widgetId, stationId, stationName, null, null);

        // Start update, could not use an AsyncTask in a BroadcastReceiver
        // (http://groups.google.com/group/android-developers/browse_thread/thread/f954ff1dc3ad5d6a)
        Intent updateIntent = new Intent(context, UpdateService.class);
        updateIntent.putExtra(WIDGET_ID, widgetId);
        updateIntent.putExtra(IClientFactoryActivity.SELECTED_STATION, stationId);
        context.startService(updateIntent);
    }

    public static class UpdateService extends IntentService {
        public UpdateService() {
            super("WidgetProvider$UpdateService");
        }

        @Override
        public void onHandleIntent(Intent intent) {
            int widgetId = intent.getIntExtra(WIDGET_ID, 0);
            String stationId = intent.getStringExtra(IClientFactoryActivity.SELECTED_STATION);
            String stationName = WidgetConfigurationActivity.loadWidgetStationName(this, widgetId);

            int retry = 0;
            while (retry <= MAX_RETRIES) {
                try {
                    String[] serverUrls = getResources().getStringArray(R.array.servers);
                    ClientFactory clientFactory = new ClientFactory(this, serverUrls);
                    StationData stationData = clientFactory.getStationData(stationId, "widget");
                    updateWidget(this, widgetId, stationId, stationName, stationData, null);
                    break;
                } catch (Exception e) {
                    retry++;
                    try {
                        Thread.sleep(RETRY_DELAY);
                    } catch (InterruptedException ignoredException) {
                        // Never mind
                    }
                }
            }
        }
    }

    public static void updateWidget(Context context, int widgetId, String stationId, String stationName, StationData stationData,
        CharSequence errorText) {
        try {
            RemoteViews views;

            if (errorText != null) {
                views = new RemoteViews(context.getPackageName(), R.layout.widget_station_error);
                views.setTextViewText(R.id.widget_error, errorText);
            } else {
                views = new RemoteViews(context.getPackageName(), R.layout.widget_station);

                Intent intent = new Intent(context, StationBrowsingActivity.class);
                intent.putExtra(IClientFactoryActivity.SELECTED_STATION, stationId);
                // Force each widget to have a unique PendingIntent
                // (http://stackoverflow.com/questions/4011178/multiple-instances-of-widget-only-updating-last-widget)
                Uri data = Uri.withAppendedPath(Uri.parse("WIDGET" + "://widget/id/"), String.valueOf(widgetId));
                intent.setData(data);

                PendingIntent pendingIntent = TaskStackBuilder.create(context).addNextIntentWithParentStack(intent)
                    .getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);

                views.setOnClickPendingIntent(R.id.widget_station, pendingIntent);
                views.setTextViewText(R.id.station_name, stationName);

                if (stationData != null) {
                    LastUpdate lastUpdate = StationDataUtils.getAbsoluteLastUpdate(stationData);
                    views.setTextColor(R.id.station_lastUpdate, lastUpdate.color);
                    views.setTextViewText(R.id.station_lastUpdate, lastUpdate.text);

                    float[] directions = stationData.getWindDirections();
                    // Takes the last value
                    float direction = directions[directions.length - 1];
                    String[] directionLabels = context.getResources().getStringArray(R.array.directions);
                    views.setTextViewText(R.id.wind_last_direction, StationDataUtils.getWindDirectionLabel(directionLabels, direction));

                    views.setTextViewText(R.id.wind_last_average, stationData.getWindAverage());
                    views.setTextViewText(R.id.wind_last_max, stationData.getWindMax());
                } else {
                    // Loading
                    views.setTextViewText(R.id.station_lastUpdate, context.getText(R.string.loading_text));
                }
            }

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views);
        } catch (Exception e) {
            Log.e("WidgetProvider", "updateWidget()", e);
        }
    }
}
