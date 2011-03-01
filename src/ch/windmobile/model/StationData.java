package ch.windmobile.model;

import java.util.Date;

public class StationData {

    private Date lastUpdate;
    private Date lastDemand;
    private Date expirationDate;
    private String status;
    private String windAverage;
    private String windMax;
    private float windTrend;
    private float[] windDirections;
    private String windHistoryMin;
    private String windHistoryAverage;
    private String windHistoryMax;
    private String airTemperature;
    private String airHumidity;

    public Date getLastUpdate() {
        return lastUpdate;
    }

    public void setLastUpdate(Date lastUpdate) {
        this.lastUpdate = lastUpdate;
    }

    public Date getLastDemand() {
        return lastDemand;
    }

    public void setLastDemand(Date lastDemand) {
        this.lastDemand = lastDemand;
    }

    public Date getExpirationDate() {
        return expirationDate;
    }

    public void setExpirationDate(Date expirationDate) {
        this.expirationDate = expirationDate;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getWindAverage() {
        return windAverage;
    }

    public void setWindAverage(String windAverage) {
        this.windAverage = windAverage;
    }

    public String getWindMax() {
        return windMax;
    }

    public void setWindMax(String windMax) {
        this.windMax = windMax;
    }

    public float getWindTrend() {
        return windTrend;
    }

    public void setWindTrend(float windTrend) {
        this.windTrend = windTrend;
    }

    public float[] getWindDirections() {
        return windDirections;
    }

    public void setWindDirections(float[] windDirections) {
        this.windDirections = windDirections;
    }

    public String getWindHistoryMin() {
        return windHistoryMin;
    }

    public void setWindHistoryMin(String windHistoryMin) {
        this.windHistoryMin = windHistoryMin;
    }

    public String getWindHistoryAverage() {
        return windHistoryAverage;
    }

    public void setWindHistoryAverage(String windHistoryAverage) {
        this.windHistoryAverage = windHistoryAverage;
    }

    public String getWindHistoryMax() {
        return windHistoryMax;
    }

    public void setWindHistoryMax(String windHistoryMax) {
        this.windHistoryMax = windHistoryMax;
    }

    public String getAirTemperature() {
        return airTemperature;
    }

    public void setAirTemperature(String airTemperature) {
        this.airTemperature = airTemperature;
    }

    public String getAirHumidity() {
        return airHumidity;
    }

    public void setAirHumidity(String airHumidity) {
        this.airHumidity = airHumidity;
    }
}
