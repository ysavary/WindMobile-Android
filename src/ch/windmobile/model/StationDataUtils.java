package ch.windmobile.model;

import java.text.DateFormat;
import java.util.List;

import android.graphics.Color;
import android.text.format.DateUtils;

public class StationDataUtils {

    public static final class LastUpdate {
        public int color;
        public CharSequence text;
    }

    public static LastUpdate getRelativeLastUpdate(StationData stationData) {
        LastUpdate lastUpdate = new LastUpdate();

        if (stationData.getStatus().equalsIgnoreCase("red")) {
            lastUpdate.color = Color.RED;
        } else if (stationData.getStatus().equalsIgnoreCase("orange")) {
            lastUpdate.color = Color.YELLOW;
        } else {
            lastUpdate.color = Color.WHITE;
        }

        CharSequence lastUpdateText = DateUtils.getRelativeTimeSpanString(stationData.getLastUpdate().getTime());
        lastUpdate.text = lastUpdateText;

        return lastUpdate;
    }

    public static LastUpdate getAbsoluteLastUpdate(StationData stationData) {
        LastUpdate lastUpdate = new LastUpdate();

        if (stationData.getStatus().equalsIgnoreCase("red")) {
            lastUpdate.color = Color.RED;
        } else if (stationData.getStatus().equalsIgnoreCase("orange")) {
            lastUpdate.color = Color.YELLOW;
        } else {
            lastUpdate.color = Color.WHITE;
        }

        CharSequence lastUpdateText = DateUtils.formatSameDayTime(stationData.getLastUpdate().getTime(), System.currentTimeMillis(),
            DateFormat.MEDIUM, DateFormat.SHORT);
        lastUpdate.text = lastUpdateText;

        return lastUpdate;
    }

    public static String getWindDirectionLabel(String[] directionLabels, float windDirection) {
        float sector = 360 / directionLabels.length;
        float directionAngle = 0;
        for (int directionIndex = 0; directionIndex < directionLabels.length; directionIndex++) {
            float min = directionAngle - sector / 2;
            float max = directionAngle + sector / 2;

            if (directionIndex == 0) {
                // Looking for the north "half sector" from 337.5 to 360
                if ((windDirection >= 360 + min) && (windDirection <= 360)) {
                    return directionLabels[0];
                }
            }
            if ((windDirection >= min) && (windDirection < max)) {
                return directionLabels[directionIndex];
            }

            directionAngle += sector;
        }
        throw new IllegalArgumentException("Wind direction label not found");
    }

    public static boolean isPeak(List<Double> values) {
        int size = values.size();
        if ((size == 0) || (size % 2 == 0)) {
            throw new IllegalArgumentException("Values[] size must be odd");
        }

        int middleIndex = size / 2;
        double maxValue = 0;
        int maxIndex = 0;

        for (int i = 0; i < values.size(); i++) {
            if (values.get(i) > maxValue) {
                maxValue = values.get(i);
                maxIndex = i;
            }
        }

        return (maxIndex == middleIndex);
    }
}
