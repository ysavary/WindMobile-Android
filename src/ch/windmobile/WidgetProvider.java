package ch.windmobile;

import android.app.IntentService;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
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
    private static final String STATION_NAME = "ch.windmobile.stationName";

    private static final int MAX_RETRIES = 3;
    private static final int RETRY_DELAY = 20 * 1000;

    @Override
    public void onUpdate(Context context, AppWidgetManager widgetManager, int[] widgetIds) {
        Log.i("WidgetProvider", "onUpdate() : " + widgetIds.length + " widget ids");

        for (int widgetId : widgetIds) {
            // Check if the widget is already configured
            String stationId;
            try {
                stationId = WidgetConfigurationActivity.loadWidgetStationId(context, widgetId);
            } catch (Exception e) {
                stationId = null;
            }
            if (stationId != null) {
                updateWidget(context, widgetId);
            } else {
                // Mark this widget as orphan
                updateStatus(context, widgetId, stationId, null, false, context.getResources().getString(R.string.widget_error));
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

    public static void updateWidget(Context context, int widgetId) {
        Log.i("WidgetProvider", "updateAppWidget() : widgetId '" + widgetId + "'");

        String stationId = WidgetConfigurationActivity.loadWidgetStationId(context, widgetId);
        String stationName = WidgetConfigurationActivity.loadWidgetStationName(context, widgetId);

        updateStatus(context, widgetId, stationId, stationName, true, null);

        // Start update, could not use an AsyncTask in a BroadcastReceiver
        // (http://groups.google.com/group/android-developers/browse_thread/thread/f954ff1dc3ad5d6a)
        Intent updateIntent = new Intent(context, UpdateService.class);
        updateIntent.putExtra(WIDGET_ID, widgetId);
        updateIntent.putExtra(IClientFactoryActivity.SELECTED_STATION, stationId);
        updateIntent.putExtra(STATION_NAME, stationName);
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
            String stationName = intent.getStringExtra(STATION_NAME);

            int retry = 0;
            while (retry <= MAX_RETRIES) {
                try {
                    String[] serverUrls = getResources().getStringArray(R.array.servers);
                    ClientFactory clientFactory = new ClientFactory(this, serverUrls);
                    StationData stationData = clientFactory.getStationData(stationId);
                    updateStatus(this, widgetId, stationId, stationName, false, null);
                    updateStationData(this, widgetId, stationData);
                    break;
                } catch (Exception e) {
                    /* Don't log exception, leave old value
                    WindMobileException clientException = WindMobile.createException(this, e);
                    if (clientException.isFatal() == false) {
                        updateStatus(this, widgetId, stationId, stationName, false, clientException.getLocalizedName());
                        break;
                    }
                    */
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

    static void updateStatus(Context context, int widgetId, String stationId, String stationName, boolean loading, CharSequence errorText) {
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_station);

            Intent intent = new Intent(context, StationBrowsingActivity.class);
            intent.setFlags(Intent.FLAG_ACTIVITY_CLEAR_TOP);
            intent.putExtra(IClientFactoryActivity.SELECTED_STATION, stationId);
            // Force each widget to have a unique PendingIntent
            // (http://stackoverflow.com/questions/4011178/multiple-instances-of-widget-only-updating-last-widget)
            Uri data = Uri.withAppendedPath(Uri.parse("WIDGET" + "://widget/id/"), String.valueOf(widgetId));
            intent.setData(data);

            views.setOnClickPendingIntent(R.id.widget_station, PendingIntent.getActivity(context, 0, intent, PendingIntent.FLAG_UPDATE_CURRENT));
            views.setTextViewText(R.id.station_name, stationName);

            if (loading) {
                views.setTextColor(R.id.station_lastUpdate, WindMobile.whiteTextColor);
                views.setTextViewText(R.id.station_lastUpdate, context.getText(R.string.loading_text));
            } else if (errorText != null) {
                views.setTextColor(R.id.station_lastUpdate, WindMobile.redTextColor);
                views.setTextViewText(R.id.station_lastUpdate, errorText);
            }

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views);
        } catch (Exception e) {
            Log.e("WidgetProvider", "updateStatus()", e);
        }
    }

    static void updateStationData(Context context, int widgetId, StationData stationData) {
        try {
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.widget_station);

            LastUpdate lastUpdate = StationDataUtils.getAbsoluteLastUpdate(stationData);
            views.setTextColor(R.id.station_lastUpdate, lastUpdate.color);
            views.setTextViewText(R.id.station_lastUpdate, lastUpdate.text);

            float[] directions = stationData.getWindDirections();
            // Takes the last value
            float direction = directions[directions.length - 1];
            String[] directionLabels = context.getResources().getStringArray(R.array.directions);
            views.setTextViewText(R.id.wind_direction, StationDataUtils.getWindDirectionLabel(directionLabels, direction));

            views.setTextViewText(R.id.wind_last_average, stationData.getWindAverage());
            views.setTextViewText(R.id.wind_last_max, stationData.getWindMax());

            AppWidgetManager.getInstance(context).updateAppWidget(widgetId, views);
        } catch (Exception e) {
            Log.e("WidgetProvider", "updateStationData()", e);
        }
    }
}
