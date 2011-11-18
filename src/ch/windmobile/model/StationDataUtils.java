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

import java.text.DateFormat;
import java.util.List;

import android.text.format.DateUtils;
import ch.windmobile.WindMobile;

public class StationDataUtils {

    public static final class LastUpdate {
        public int color;
        public CharSequence text;
    }

    public static LastUpdate getRelativeLastUpdate(StationData stationData) {
        LastUpdate lastUpdate = new LastUpdate();

        if (stationData.getStatus().equalsIgnoreCase(StationInfo.STATUS_RED)) {
            lastUpdate.color = WindMobile.redTextColor;
        } else if (stationData.getStatus().equalsIgnoreCase(StationInfo.STATUS_ORANGE)) {
            lastUpdate.color = WindMobile.orangeTextColor;
        } else {
            lastUpdate.color = WindMobile.whiteTextColor;
        }

        CharSequence lastUpdateText = DateUtils.getRelativeTimeSpanString(stationData.getLastUpdate().getTime());
        lastUpdate.text = lastUpdateText;

        return lastUpdate;
    }

    public static LastUpdate getAbsoluteLastUpdate(StationData stationData) {
        LastUpdate lastUpdate = new LastUpdate();

        if (stationData.getStatus().equalsIgnoreCase(StationInfo.STATUS_RED)) {
            lastUpdate.color = WindMobile.redTextColor;
        } else if (stationData.getStatus().equalsIgnoreCase(StationInfo.STATUS_ORANGE)) {
            lastUpdate.color = WindMobile.orangeTextColor;
        } else {
            lastUpdate.color = WindMobile.whiteTextColor;
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
        int middleIndex = values.size() / 2;
        double middleValue = values.get(middleIndex);
        double maxValue = 0;
        int maxIndex = 0;

        for (int i = 0; i < values.size(); i++) {
            double currentValue = values.get(i);
            if (currentValue > middleValue) {
                return false;
            }

            // Mark the 1st max value only if the chart contains a "flat" max
            if (currentValue > maxValue) {
                maxValue = currentValue;
                maxIndex = i;
            }
        }
        return (maxIndex == middleIndex);
    }
}
