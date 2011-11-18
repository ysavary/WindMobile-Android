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
package ch.windmobile.controller;

import java.util.List;

public class CircularController {

    private List<String> stationIds;
    private String currentStationId;

    public CircularController() {
    }

    public CircularController(List<String> stationIds) {
        this.stationIds = stationIds;
    }

    public String getCurrentStationId() {
        return currentStationId;
    }

    public void setCurrentStationId(String stationId) {
        currentStationId = stationId;
    }

    public List<String> getStationIds() {
        return stationIds;
    }

    public void setStationIds(List<String> stationIds) {
        this.stationIds = stationIds;
    }

    public String getNextStationId() {
        List<String> ids = getStationIds();

        int index = ids.indexOf(currentStationId);
        if (index == -1) {
            throw new IndexOutOfBoundsException("Unable to find currentStationId");
        }
        index++;
        if (index == ids.size()) {
            index = 0;
        }
        return ids.get(index);
    }

    public String getPreviousStationId() {
        List<String> ids = getStationIds();

        int index = ids.indexOf(currentStationId);
        if (index == -1) {
            throw new IndexOutOfBoundsException("Unable to find currentStationId");
        }
        index--;
        if (index == -1) {
            index = ids.size() - 1;
        }
        return ids.get(index);
    }

    public String nextStation() {
        currentStationId = getNextStationId();
        return currentStationId;
    }

    public String previousStation() {
        currentStationId = getPreviousStationId();
        return currentStationId;
    }
}
