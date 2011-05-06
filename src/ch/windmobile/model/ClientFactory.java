package ch.windmobile.model;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import org.apache.http.client.ClientProtocolException;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import android.content.Context;
import android.preference.PreferenceManager;
import android.util.Log;
import ch.windmobile.R;
import ch.windmobile.WindMobile;

public class ClientFactory {
    private static final DateFormat _dateTimeFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
    private static final long RED_STATUS_WAITING_TIME = 15 * 60 * 1000;
    private static final long WAITING_TIME = 1 * 60 * 1000;

    private final RestClient restClient;
    private final Map<String, StationInfo> stationInfosCache = Collections.synchronizedMap(new LinkedHashMap<String, StationInfo>());
    private final Map<String, StationData> stationDataCache = new ConcurrentHashMap<String, StationData>();

    private final Context context;
    private final String[] urls;

    public ClientFactory(Context context, String[] urls) {
        this.context = context;
        this.urls = urls;
        int networkTimeout = WindMobile.readNetworkTimeout(context);
        restClient = new RestClient(context, networkTimeout * 1000, networkTimeout * 1000);
        restClient.setUserAgent(WindMobile.getUserAgent(context));
    }

    public boolean needStationInfosUpdate() {
        return (stationInfosCache.size() == 0);
    }

    public List<StationInfo> getStationInfosCache() {
        return new ArrayList<StationInfo>(stationInfosCache.values());
    }

    public StationInfo getStationInfoCache(String stationId) {
        return stationInfosCache.get(stationId);
    }

    public void clear() {
        stationInfosCache.clear();
        stationDataCache.clear();
    }

    private StationInfo createStationInfo(JSONObject stationInfoJson) throws JSONException {
        StationInfo stationInfo = new StationInfo();

        stationInfo.setId(stationInfoJson.getString("@id"));
        stationInfo.setShortName(stationInfoJson.getString("@shortName"));
        stationInfo.setName(stationInfoJson.getString("@name"));
        stationInfo.setAltitude(stationInfoJson.getString("@altitude"));

        double latitude = stationInfoJson.getDouble("@wgs84Latitude");
        double longitude = stationInfoJson.getDouble("@wgs84Longitude");
        stationInfo.setLatitude((int) (latitude * 1E6));
        stationInfo.setLongitude((int) (longitude * 1E6));

        stationInfo.setMaintenanceStatus(stationInfoJson.getString("@maintenanceStatus"));
        stationInfo.setFavorite(WindMobile.readFavoriteStationIds(context).contains(stationInfo.getId()));

        return stationInfo;
    }

    public List<StationInfo> getStationInfos(boolean operationalStationOnly) throws IOException, JSONException, WindMobileException {
        String serverUrl;
        if (operationalStationOnly == false) {
            serverUrl = createServerUrl("stationinfos", "?allStation=true");
        } else {
            serverUrl = createServerUrl("stationinfos");
        }

        JSONObject stationInfosJson;
        try {
            stationInfosJson = restClient.get(serverUrl);
        } catch (JSONException e) {
            throw new ClientException(getContext().getResources().getText(R.string.data_error), "No data");
        }

        // Clear stations cache
        clear();

        JSONArray stationInfoListJson = stationInfosJson.optJSONArray("stationInfo");
        if (stationInfoListJson != null) {
            for (int i = 0; i < stationInfoListJson.length(); i++) {
                try {
                    StationInfo stationInfo = createStationInfo(stationInfoListJson.getJSONObject(i));
                    stationInfosCache.put(stationInfo.getId(), stationInfo);
                } catch (JSONException e) {
                    Log.e("WindMobile", "ClientFactory() --> Ignored station", e);
                }
            }
        } else {
            JSONObject stationInfoJson = stationInfosJson.optJSONObject("stationInfo");
            if (stationInfoJson != null) {
                StationInfo stationInfo = createStationInfo(stationInfoJson);
                stationInfosCache.put(stationInfo.getId(), stationInfo);
            }
        }

        if (stationInfosCache.size() == 0) {
            new ClientException(getContext().getResources().getText(R.string.data_error), "No station");
        }

        List<StationInfo> list = new ArrayList<StationInfo>(stationInfosCache.values());
        return Collections.unmodifiableList(list);
    }

    public boolean needStationDataUpdate(StationData stationData) {
        if (stationData == null) {
            return true;
        }

        Date expirationDate = stationData.getExpirationDate();
        Date now = new Date();
        boolean expired = (now.after(expirationDate));
        if (expired) {
            if (stationData.getLastDemand().before(expirationDate)) {
                return true;
            }
            // We already asked the new data after the expiration date, wait
            // some time before a new demand
            long waitedTime = now.getTime() - stationData.getLastDemand().getTime();
            if (stationData.getStatus().equalsIgnoreCase(StationInfo.STATUS_RED)) {
                if (waitedTime >= RED_STATUS_WAITING_TIME) {
                    return true;
                }
            } else {
                if (waitedTime >= WAITING_TIME) {
                    return true;
                }
            }
        }
        return false;
    }

    public StationData getStationDataCache(String stationId) {
        if (stationInfosCache.containsKey(stationId)) {
            StationData stationData = stationDataCache.get(stationId);
            return stationData;
        }
        return null;
    }

    public StationData getStationData(String stationId) throws IOException, JSONException, ParseException, WindMobileException {
        if ((stationId == null) || (stationId.equals(""))) {
            throw new ClientException(context.getText(R.string.data_error), "Invalid id");
        }

        String serverUrl = createServerUrl("stationdatas/" + stationId);
        JSONObject stationDataJson = restClient.get(serverUrl);

        StationData stationData = new StationData();
        // DateFormat are NOT thread safe
        DateFormat dateTimeFormat = (DateFormat) _dateTimeFormat.clone();
        Date lastUpdate = dateTimeFormat.parse(stationDataJson.getString("@lastUpdate"));
        stationData.setLastUpdate(lastUpdate);
        Date expirationDate = dateTimeFormat.parse(stationDataJson.getString("@expirationDate"));
        stationData.setExpirationDate(expirationDate);
        stationData.setStatus(stationDataJson.getString("@status"));

        stationData.setWindAverage(stationDataJson.getString("windAverage"));
        stationData.setWindMax(stationDataJson.getString("windMax"));

        JSONArray windDirectionPoints = stationDataJson.getJSONObject("windDirectionChart").getJSONObject("serie").getJSONArray("points");
        float[] windDirections = new float[windDirectionPoints.length()];
        for (int i = 0; i < windDirectionPoints.length(); i++) {
            double value = windDirectionPoints.getJSONObject(i).getDouble("value");
            windDirections[i] = (float) value;
        }
        stationData.setWindDirections(windDirections);

        stationData.setWindTrend((float) (stationDataJson.getDouble("windTrend")));

        stationData.setWindHistoryMin(stationDataJson.getString("windHistoryMin"));
        stationData.setWindHistoryAverage(stationDataJson.getString("windHistoryAverage"));
        stationData.setWindHistoryMax(stationDataJson.getString("windHistoryMax"));

        stationData.setAirTemperature(stationDataJson.getString("airTemperature"));
        stationData.setAirHumidity(stationDataJson.getString("airHumidity"));

        stationData.setLastDemand(new Date());

        stationDataCache.put(stationId, stationData);
        return stationData;
    }

    public static final class WindChartData {
        public JSONArray windAverage;
        public JSONArray windMax;
        public JSONArray windDirection;
    }

    public WindChartData getWindChart(String stationId, int duration) throws IOException, JSONException, WindMobileException {
        String serverUrl = createServerUrl("windchart/" + stationId + "/" + duration);
        JSONObject windChartJson = restClient.get(serverUrl);

        JSONArray series = windChartJson.getJSONArray("serie");

        WindChartData windChartData = new WindChartData();
        windChartData.windAverage = series.getJSONObject(0).getJSONArray("points");
        windChartData.windMax = series.getJSONObject(1).getJSONArray("points");
        windChartData.windDirection = series.getJSONObject(2).getJSONArray("points");

        return windChartData;
    }

    private String createMessage(JSONObject messageJson) throws ParseException, JSONException {
        final DateFormat dateTimeFormat = new SimpleDateFormat("HH:mm:ss");
        String result = "";

        Date date = _dateTimeFormat.parse(messageJson.getString("date"));
        result += dateTimeFormat.format(date);
        result += " (" + messageJson.getString("pseudo") + ") : ";
        result += messageJson.getString("text");

        return result;
    }

    public String getLastMessages() throws ServerException, ClientProtocolException, IOException, JSONException {
        final String chatRoomId = "test";

        String serverUrl = createServerUrl("chatrooms/" + chatRoomId + "/lastmessages/" + 5);
        JSONObject messagesJson;
        try {
            messagesJson = restClient.get(serverUrl);
        } catch (JSONException e) {
            return "";
        }

        String result = "";
        JSONArray messageListJson = messagesJson.optJSONArray("message");
        if (messageListJson != null) {
            for (int i = 0; i < messageListJson.length(); i++) {
                try {
                    result += createMessage(messageListJson.getJSONObject(i)) + "\n";
                } catch (Exception e) {
                    Log.e("WindMobile", "ClientFactory() --> Ignored message", e);
                }
            }
        } else {
            JSONObject messageJson = messagesJson.optJSONObject("message");
            if (messageJson != null) {
                try {
                    result += createMessage(messageJson) + "\n";
                } catch (ParseException e) {
                    Log.e("WindMobile", "ClientFactory() --> Ignored message", e);
                }
            }
        }

        return result;
    }

    public void postMessage(String message) throws ServerException, ClientProtocolException, IOException, JSONException {
        final String chatRoomId = "test";
        final String username = PreferenceManager.getDefaultSharedPreferences(context).getString("username", "");
        final String password = PreferenceManager.getDefaultSharedPreferences(context).getString("password", "");

        String serverUrl = createServerUrl("chatrooms/" + chatRoomId + "/postmessage");
        restClient.post(serverUrl, message, username, password);
    }

    public String getUrl() {
        return urls[0];
    }

    private String createServerUrl(String path) {
        return getUrl() + path;
    }

    private String createServerUrl(String path, String queryString) {
        if ((queryString != null) && (queryString.equals("") == false)) {
            return getUrl() + path + queryString;
        } else {
            return createServerUrl(path);
        }
    }

    public List<String> getStationIds() {
        Set<String> keys = stationInfosCache.keySet();
        return new ArrayList<String>(keys);
    }

    public void testException() {
        try {
            String serverUrl = createServerUrl("test/exception");
            restClient.get(serverUrl);
        } catch (Exception e) {
            // This is only a test, logging is done by RestClient class
        }
    }

    public Context getContext() {
        return context;
    }
}
