package ch.windmobile.model;

public class StationInfo {
    public static String STATUS_GREEN = "green";
    public static String STATUS_ORANGE = "orange";
    public static String STATUS_RED = "red";

    private String id;
    private String shortName;
    private String name;
    private String altitude;
    private int latitude;
    private int longitude;
    private String maintenanceStatus;

    private boolean favorite;

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getShortName() {
        return shortName;
    }

    public void setShortName(String shortName) {
        this.shortName = shortName;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getAltitude() {
        return altitude;
    }

    public void setAltitude(String altitude) {
        this.altitude = altitude;
    }

    public int getLatitude() {
        return latitude;
    }

    public void setLatitude(int latitude) {
        this.latitude = latitude;
    }

    public int getLongitude() {
        return longitude;
    }

    public void setLongitude(int longitude) {
        this.longitude = longitude;
    }

    public String getMaintenanceStatus() {
        return maintenanceStatus;
    }

    public void setMaintenanceStatus(String maintenanceStatus) {
        this.maintenanceStatus = maintenanceStatus;
    }

    public boolean isFavorite() {
        return favorite;
    }

    public void setFavorite(boolean favorite) {
        this.favorite = favorite;
    }
}
