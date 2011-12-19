/*******************************************************************************
 * Copyright (c) 2011 epyx SA.
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU Affero General Public License as published by the Free
 * Software Foundation, either version 3 of the License, or (at your option) any
 * later version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE.  See the GNU Affero General Public License for more
 * details.
 *
 * You should have received a copy of the GNU Affero General Public License along
 * with this program.  If not, see <http://www.gnu.org/licenses/>.
 *******************************************************************************/
package ch.windmobile.model;

import java.io.IOException;
import java.text.DateFormat;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.LinkedHashSet;
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
    private static final ThreadLocal<DateFormat> dateTimeFormat = new ThreadLocal<DateFormat>() {
        @Override
        protected DateFormat initialValue() {
            return new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ssZ");
        }
    };
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

        // Get favorites from server
        try {
            for (String id : getFavorites()) {
                StationInfo stationInfo = stationInfosCache.get(id);
                if (stationInfo != null) {
                    stationInfo.setFavorite(true);
                }
            }
        } catch (Exception e) {
            Log.e("WindMobile", "ClientFactory() --> Unable to get favorites from server", e);
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
        Date lastUpdate = dateTimeFormat.get().parse(stationDataJson.getString("@lastUpdate"));
        stationData.setLastUpdate(lastUpdate);
        Date expirationDate = dateTimeFormat.get().parse(stationDataJson.getString("@expirationDate"));
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

    private Message createMessage(JSONObject messageJson) throws ParseException, JSONException {
        Date date = dateTimeFormat.get().parse(messageJson.getString("date"));
        return new Message(date, messageJson.getString("pseudo"), messageJson.getString("text"), messageJson.getString("emailHash"));
    }

    public List<Message> getLastMessages(String chatRoom, int numberOfMessages) throws ServerException, ClientProtocolException, IOException,
        JSONException {
        ArrayList<Message> result = new ArrayList<Message>();

        String serverUrl = createServerUrl("chatrooms/" + chatRoom, "?maxCount=" + numberOfMessages);
        JSONObject messagesJson;
        try {
            messagesJson = restClient.get(serverUrl);
        } catch (JSONException e) {
            return result;
        }

        JSONArray messageListJson = messagesJson.optJSONArray("message");
        if (messageListJson != null) {
            for (int i = 0; i < messageListJson.length(); i++) {
                try {
                    result.add(createMessage(messageListJson.getJSONObject(i)));
                } catch (Exception e) {
                    Log.e("WindMobile", "ClientFactory() --> Ignored message", e);
                }
            }
        } else {
            JSONObject messageJson = messagesJson.optJSONObject("message");
            if (messageJson != null) {
                try {
                    result.add(createMessage(messageJson));
                } catch (ParseException e) {
                    Log.e("WindMobile", "ClientFactory() --> Ignored message", e);
                }
            }
        }

        return result;
    }

    public List<Long> getLastMessageIds(List<String> chatRoomIds) throws ServerException, ClientProtocolException, IOException, JSONException {
        List<Long> result = new ArrayList<Long>();

        String serverUrl = createServerUrl("chatrooms");
        if (chatRoomIds.size() > 0) {
            serverUrl += "?";
            for (String chatRoomId : chatRoomIds) {
                serverUrl += "chatroom=";
                serverUrl += chatRoomId;
                serverUrl += "&";
            }
        }

        JSONObject messagesJson;
        try {
            messagesJson = restClient.get(serverUrl);
        } catch (JSONException e) {
            return result;
        }

        JSONArray messageListJson = messagesJson.optJSONArray("messageId");
        if (messageListJson != null) {
            for (int i = 0; i < messageListJson.length(); i++) {
                try {
                    result.add(messageListJson.getLong(i));
                } catch (Exception e) {
                    Log.e("WindMobile", "ClientFactory() --> Ignored message", e);
                }
            }
        } else {
            JSONObject messageJson = messagesJson.optJSONObject("messageId");
            if (messageJson != null) {
                // try {
                result.add(messageJson.getLong(""));
                // } catch (ParseException e) {
                // Log.e("WindMobile", "ClientFactory() --> Ignored message", e);
                // }
            }
        }

        return result;
    }

    public void postMessage(String chatRoomId, String message) throws ServerException, ClientProtocolException, IOException, JSONException {
        final String username = PreferenceManager.getDefaultSharedPreferences(context).getString("username", "");
        final String password = PreferenceManager.getDefaultSharedPreferences(context).getString("password", "");

        String serverUrl = createServerUrl("chatrooms/" + chatRoomId);
        restClient.post(serverUrl, message, username, password);
    }

    public Set<String> getFavorites() throws ServerException, ClientProtocolException, IOException, JSONException {
        final String username = PreferenceManager.getDefaultSharedPreferences(context).getString("username", "");
        final String password = PreferenceManager.getDefaultSharedPreferences(context).getString("password", "");

        String serverUrl = createServerUrl("users/current/favorites");
        JSONObject favorites;
        try {
            favorites = restClient.get(serverUrl, username, password);
        } catch (JSONException e) {
            return new HashSet<String>();
        }

        Set<String> result = new LinkedHashSet<String>();
        JSONArray favoritesList = favorites.optJSONArray("stationId");
        if (favoritesList != null) {
            for (int i = 0; i < favoritesList.length(); i++) {
                result.add(favoritesList.getString(i));
            }
        } else {
            JSONObject favorite = favorites.optJSONObject("stationId");
            if (favorite != null) {
                result.add(favorite.toString());
            }
        }

        return result;
    }

    public void addToFavorites(String stationId) throws ServerException, ClientProtocolException, IOException, JSONException {
        final String username = PreferenceManager.getDefaultSharedPreferences(context).getString("username", "");
        final String password = PreferenceManager.getDefaultSharedPreferences(context).getString("password", "");

        String serverUrl = createServerUrl("users/current/favorites/" + stationId);
        restClient.put(serverUrl, null, username, password);
    }

    public void removeFromFavorites(String stationId) throws ServerException, ClientProtocolException, IOException, JSONException {
        final String username = PreferenceManager.getDefaultSharedPreferences(context).getString("username", "");
        final String password = PreferenceManager.getDefaultSharedPreferences(context).getString("password", "");

        String serverUrl = createServerUrl("users/current/favorites/" + stationId);
        restClient.delete(serverUrl, username, password);
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
