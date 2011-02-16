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
