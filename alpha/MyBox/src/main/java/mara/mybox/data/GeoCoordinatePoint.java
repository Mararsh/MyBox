package mara.mybox.data;

/**
 * @Author Mara
 * @CreateDate 2022-10-2
 * @License Apache License Version 2.0
 */
public class GeoCoordinatePoint {

    protected GeoCoordinateSystem geoCoordinateSystem;
    protected float longitude, latitude, altitude;

    public GeoCoordinatePoint() {
        longitude = Float.NaN;
        latitude = Float.NaN;
        altitude = Float.NaN;
        geoCoordinateSystem = GeoCoordinateSystem.defaultCode();
    }

    public GeoCoordinatePoint(float longitude, float latitude) {
        this.longitude = longitude;
        this.latitude = latitude;
    }

    public GeoCoordinatePoint(float longitude, float latitude, float altitude) {
        this.longitude = longitude;
        this.latitude = latitude;
        this.altitude = altitude;
    }

    public boolean valid() {
        return longitude >= -180 && longitude <= 180 && latitude >= -90 && latitude <= 90;
    }

    /*
        static
     */
    public static GeoCoordinatePoint create() {
        return new GeoCoordinatePoint();
    }

    public static GeoCoordinatePoint parse(String string, String separator) {
        try {
            if (string == null || string.isBlank()) {
                return null;
            }
            String[] vs = string.split(separator);
            if (vs == null || vs.length < 2) {
                return null;
            }
            if (vs.length > 2) {
                return new GeoCoordinatePoint(Float.parseFloat(vs[0]), Float.parseFloat(vs[1]), Float.parseFloat(vs[2]));
            } else {
                return new GeoCoordinatePoint(Float.parseFloat(vs[0]), Float.parseFloat(vs[1]));
            }
        } catch (Exception e) {
            return null;
        }
    }

    public static boolean valid(String string, String separator) {
        GeoCoordinatePoint p = parse(string, separator);
        return p != null && p.valid();
    }

    /*
        get/set
     */
    public GeoCoordinateSystem getGeoCoordinateSystem() {
        return geoCoordinateSystem;
    }

    public GeoCoordinatePoint setGeoCoordinateSystem(GeoCoordinateSystem geoCoordinateSystem) {
        this.geoCoordinateSystem = geoCoordinateSystem;
        return this;
    }

    public float getLongitude() {
        return longitude;
    }

    public GeoCoordinatePoint setLongitude(float longitude) {
        this.longitude = longitude;
        return this;
    }

    public float getLatitude() {
        return latitude;
    }

    public GeoCoordinatePoint setLatitude(float latitude) {
        this.latitude = latitude;
        return this;
    }

    public float getAltitude() {
        return altitude;
    }

    public GeoCoordinatePoint setAltitude(float altitude) {
        this.altitude = altitude;
        return this;
    }

}
