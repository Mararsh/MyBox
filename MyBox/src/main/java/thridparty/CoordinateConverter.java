package thridparty;

import mara.mybox.tools.DoubleTools;

/**
 * @Author https://www.jianshu.com/p/c39a2c72dc65?from=singlemessage
 *
 * #### Changed by Mara
 */
public class CoordinateConverter {

    public static boolean outOfChina(double lon, double lat) {
        return lon < 72.004 || lon > 137.8347 || lat < 0.8293 || lat > 55.8271;
    }

    public static double[] offsets(double lon, double lat) {
        double a = 6378245.0;
        double ee = 0.00669342162296594323;
        double x = lon - 105.0, y = lat - 35.0;

        double dLon = 300.0 + x + 2.0 * y + 0.1 * x * x + 0.1 * x * y + 0.1
                * Math.sqrt(Math.abs(x));
        dLon += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        dLon += (20.0 * Math.sin(x * Math.PI) + 40.0 * Math.sin(x / 3.0 * Math.PI)) * 2.0 / 3.0;
        dLon += (150.0 * Math.sin(x / 12.0 * Math.PI) + 300.0 * Math.sin(x / 30.0
                * Math.PI)) * 2.0 / 3.0;

        double dLat = -100.0 + 2.0 * x + 3.0 * y + 0.2 * y * y + 0.1 * x * y
                + 0.2 * Math.sqrt(Math.abs(x));
        dLat += (20.0 * Math.sin(6.0 * x * Math.PI) + 20.0 * Math.sin(2.0 * x * Math.PI)) * 2.0 / 3.0;
        dLat += (20.0 * Math.sin(y * Math.PI) + 40.0 * Math.sin(y / 3.0 * Math.PI)) * 2.0 / 3.0;
        dLat += (160.0 * Math.sin(y / 12.0 * Math.PI) + 320 * Math.sin(y * Math.PI / 30.0)) * 2.0 / 3.0;

        double radLat = lat / 180.0 * Math.PI;
        double magic = Math.sin(radLat);
        magic = 1 - ee * magic * magic;
        double sqrtMagic = Math.sqrt(magic);
        dLon = (dLon * 180.0) / (a / sqrtMagic * Math.cos(radLat) * Math.PI);
        dLat = (dLat * 180.0) / ((a * (1 - ee)) / (magic * sqrtMagic) * Math.PI);
        double[] offsets = {dLon, dLat};
        return offsets;
    }

    public static double[] GCJ02ToWGS84(double lon, double lat) {
        double longitude, latitude;
        if (outOfChina(lon, lat)) {
            longitude = lon;
            latitude = lat;
        } else {
            double[] offsets = offsets(lon, lat);
            longitude = lon - offsets[0];
            latitude = lat - offsets[1];
        }
        double[] coordinate = {DoubleTools.scale(longitude, 6), DoubleTools.scale(latitude, 6)};
        return coordinate;
    }

    public static double[] WGS84ToGCJ02(double lon, double lat) {
        double longitude, latitude;
        if (outOfChina(lon, lat)) {
            longitude = lon;
            latitude = lat;
        } else {
            double[] offsets = offsets(lon, lat);
            longitude = lon + offsets[0];
            latitude = lat + offsets[1];
        }
        double[] coordinate = {DoubleTools.scale(longitude, 6), DoubleTools.scale(latitude, 6)};
        return coordinate;
    }

    public static double[] BD09ToGCJ02(double bd_lon, double bd_lat) {
        double x = bd_lon - 0.0065, y = bd_lat - 0.006;
        double z = Math.sqrt(x * x + y * y) - 0.00002 * Math.sin(y * Math.PI);
        double theta = Math.atan2(y, x) - 0.000003 * Math.cos(x * Math.PI);
        double longitude = z * Math.cos(theta);
        double latitude = z * Math.sin(theta);
        double[] coordinate = {DoubleTools.scale(longitude, 6), DoubleTools.scale(latitude, 6)};
        return coordinate;
    }

    public static double[] GCJ02ToBD09(double gg_lon, double gg_lat) {
        double x = gg_lon, y = gg_lat;
        double z = Math.sqrt(x * x + y * y) + 0.00002 * Math.sin(y * Math.PI);
        double theta = Math.atan2(y, x) + 0.000003 * Math.cos(x * Math.PI);
        double longitude = z * Math.cos(theta) + 0.0065;
        double latitude = z * Math.sin(theta) + 0.006;
        double[] coordinate = {DoubleTools.scale(longitude, 6), DoubleTools.scale(latitude, 6)};
        return coordinate;
    }

}
