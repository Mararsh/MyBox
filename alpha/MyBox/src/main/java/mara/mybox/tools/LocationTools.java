package mara.mybox.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import mara.mybox.data.GeoCoordinateSystem;
import static mara.mybox.data.GeoCoordinateSystem.Value.GCJ_02;
import mara.mybox.db.data.GeographyCodeTools;
import mara.mybox.db.data.Location;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.UserConfig;
import thridparty.CoordinateConverter;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationTools {

    public static File tiandituFile(boolean geodetic) {
        try {
            File map = FxFileTools.getInternalFile("/js/tianditu.html", "js", "tianditu.html");
            String html = TextFileTools.readTexts(map);
            html = html.replace("0ddeb917def62b4691500526cc30a9b1", UserConfig.getString("TianDiTuWebKey", AppValues.TianDiTuWebKey));
            if (geodetic) {
                html = html.replace("'EPSG:900913", "EPSG:4326");
            }
            TextFileTools.writeFile(map, html);
            return map;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return null;
        }
    }

    public static String gaodeMap() {
        try {
            File map = FxFileTools.getInternalFile("/js/GaoDeMap.html", "js", "GaoDeMap.html");
            String html = TextFileTools.readTexts(map);
            html = html.replace("06b9e078a51325a843dfefd57ffd876c", UserConfig.getString("GaoDeMapWebKey", AppValues.GaoDeMapWebKey));
            return html;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
            return "";
        }
    }

    public static boolean validCoordinate(double longitude, double latitude) {
        return longitude >= -180 && longitude <= 180
                && latitude >= -90 && latitude <= 90;
    }

    public static double[] parseDMS(String value) {
        double[] dms = {-200d, 0d, 0d, 0d};
        if (value == null || value.trim().isBlank()) {
            return dms;
        }
        try {
            String s = value.trim().toLowerCase();
            boolean negative = false, latitude = false;
            if (s.startsWith("s")) {
                negative = true;
                latitude = true;
                s = s.substring(1);
            } else if (s.startsWith("南纬")) {
                negative = true;
                latitude = true;
                s = s.substring(2);
            } else if (s.startsWith("南")) {
                negative = true;
                latitude = true;
                s = s.substring(1);
            } else if (s.endsWith("s")) {
                negative = true;
                latitude = true;
                s = s.substring(0, s.length() - 1);
            } else if (s.startsWith("n")) {
                negative = false;
                latitude = true;
                s = s.substring(1);
            } else if (s.startsWith("北纬")) {
                negative = false;
                latitude = true;
                s = s.substring(2);
            } else if (s.startsWith("北")) {
                negative = false;
                latitude = true;
                s = s.substring(1);
            } else if (s.endsWith("n")) {
                negative = false;
                latitude = true;
                s = s.substring(0, s.length() - 1);
            } else if (s.startsWith("w")) {
                negative = true;
                s = s.substring(1);
            } else if (s.startsWith("西经")) {
                negative = true;
                s = s.substring(2);
            } else if (s.startsWith("西")) {
                negative = true;
                s = s.substring(1);
            } else if (s.endsWith("w")) {
                negative = true;
                s = s.substring(0, s.length() - 1);
            } else if (s.startsWith("e")) {
                negative = false;
                s = s.substring(1);
            } else if (s.startsWith("东经")) {
                negative = false;
                s = s.substring(2);
            } else if (s.startsWith("东")) {
                negative = false;
                s = s.substring(1);
            } else if (s.endsWith("e")) {
                negative = false;
                s = s.substring(0, s.length() - 1);
            } else if (s.startsWith("纬度")) {
                latitude = true;
                s = s.substring(2);
            } else if (s.startsWith("latitude")) {
                latitude = true;
                s = s.substring("latitude".length());
            } else if (s.startsWith("经度")) {
                latitude = false;
                s = s.substring(2);
            } else if (s.startsWith("longitude")) {
                latitude = false;
                s = s.substring("longitude".length());
            }
            s = s.trim();
            if (s.startsWith("-")) {
                negative = true;
                s = s.substring(1);
            } else if (s.startsWith("+")) {
                negative = false;
                s = s.substring(1);
            }
            s = s.trim();
            int pos = s.indexOf("度");
            if (pos < 0) {
                pos = s.indexOf("°");
            }
            if (pos >= 0) {
                try {
                    int v = Integer.parseInt(s.substring(0, pos).trim());
                    if (latitude) {
                        if (v >= -90 && v <= 90) {
                            dms[1] = v;
                        } else {
                            dms[0] = -200;
                            return dms;
                        }
                    } else {
                        if (v >= -180 && v <= 180) {
                            dms[1] = v;
                        } else {
                            dms[0] = -200;
                            return dms;
                        }
                    }
                    if (pos == s.length() - 1) {
                        dms[2] = 0;
                        dms[3] = 0;
                        dms[0] = DMS2Coordinate(negative, dms[1], dms[2], dms[3]);
                        return dms;
                    }
                    s = s.substring(pos + 1).trim();
                } catch (Exception e) {
                    dms[0] = -200;
                    return dms;
                }
            } else {
                dms[1] = 0;
            }
            pos = s.indexOf("分");
            if (pos < 0) {
                pos = s.indexOf("'");
            }
            if (pos >= 0) {
                try {
                    int v = Integer.parseInt(s.substring(0, pos).trim());
                    if (v >= 0 && v < 60) {
                        dms[2] = v;
                    } else {
                        dms[0] = -200;
                        return dms;
                    }
                    if (pos == s.length() - 1) {
                        dms[3] = 0;
                        dms[0] = DMS2Coordinate(negative, dms[1], dms[2], dms[3]);
                        return dms;
                    }
                    s = s.substring(pos + 1).trim();
                } catch (Exception e) {
                    dms[0] = -200;
                    return dms;
                }
            } else {
                dms[2] = 0;
            }
            if (s.endsWith("\"") || s.endsWith("秒")) {
                s = s.substring(0, s.length() - 1);
            }
            try {
                double v = Double.parseDouble(s.trim());
                if (v >= 0 && v < 60) {
                    dms[3] = v;
                    dms[0] = DMS2Coordinate(negative, dms[1], dms[2], dms[3]);
                    return dms;
                } else {
                    dms[0] = -200;
                    return dms;
                }
            } catch (Exception e) {
                dms[0] = -200;
                return dms;
            }
        } catch (Exception e) {
            dms[0] = -200;
            return dms;
        }
    }

    public static double DMS2Coordinate(double degrees, double minutes, double seconds) {
        return DMS2Coordinate(degrees >= 0, degrees, minutes, seconds);
    }

    public static double DMS2Coordinate(boolean negitive, double degrees, double minutes, double seconds) {
        double value = Math.abs(degrees) + minutes / 60d + seconds / 3600d;
        return negitive ? -value : value;
    }

    public static double[] coordinate2DMS(double value) {
        double[] dms = new double[3];
        int i = (int) value;
        dms[0] = i;
        double d = (Math.abs(value) - Math.abs(i)) * 60d;
        i = (int) d;
        dms[1] = i;
        dms[2] = (d - i) * 60d;
        return dms;
    }

    public static String coordinateToDmsString(double value) {
        double[] dms = coordinate2DMS(value);
        return dmsString(dms[0], dms[1], dms[2]);
    }

    public static String longitudeToDmsString(double value) {
        double[] dms = coordinate2DMS(value);
        int degrees = (int) dms[0];
        String s = Math.abs(degrees) + "°" + (int) dms[1] + "'" + DoubleTools.scale(dms[2], 4) + "\"";
        return s + (degrees >= 0 ? "E" : "W");
    }

    public static String latitudeToDmsString(double value) {
        double[] dms = coordinate2DMS(value);
        int degrees = (int) dms[0];
        String s = Math.abs(degrees) + "°" + (int) dms[1] + "'" + DoubleTools.scale(dms[2], 4) + "\"";
        return s + (degrees >= 0 ? "N" : "S");
    }

    public static String dmsString(double degrees, double minutes, double seconds) {
        return (int) degrees + "°" + (int) minutes + "'" + DoubleTools.scale(seconds, 4) + "\"";
    }

    /*
        Convert
     */
    public static Location toCGCS2000(Location location) {
        Location converted = toWGS84(location);
//        if (converted != null) {
//            converted.setCoordinateSystem(CoordinateSystem.CGCS2000());
//        }
        return converted;
    }

    public static List<Location> toCGCS2000(List<Location> locations) {
        if (locations == null) {
            return locations;
        }
        List<Location> newLocations = new ArrayList<>();
        for (Location location : locations) {
            if (!Location.valid(location)) {
                continue;
            }
            Location newLocation = toCGCS2000(location);
            if (newLocation != null) {
                newLocations.add(newLocation);
            }
        }
        return newLocations;
    }

    public static Location toWGS84(Location location) {
        try {
            if (location == null || !Location.valid(location)
                    || location.getCoordinateSystem() == null) {
                return location;
            }
            GeoCoordinateSystem cs = location.getCoordinateSystem();
            double[] coordinate;
            switch (cs.getValue()) {
                case GCJ_02:
                    coordinate = CoordinateConverter.GCJ02ToWGS84(location.getLongitude(), location.getLatitude());
                    break;
                case BD_09:
                    coordinate = CoordinateConverter.BD09ToGCJ02(location.getLongitude(), location.getLatitude());
                    coordinate = CoordinateConverter.GCJ02ToWGS84(coordinate[0], coordinate[1]);
                    break;
                case Mapbar:
                    coordinate = GeographyCodeTools.toGCJ02ByWebService(location.getCoordinateSystem(),
                            location.getLongitude(), location.getLatitude());
                    coordinate = CoordinateConverter.GCJ02ToWGS84(coordinate[0], coordinate[1]);
                    break;
                case CGCS2000:
                case WGS_84:
                default:
                    return location;
            }
            Location newCode = (Location) location.clone();
            newCode.setLdid(-1);
            newCode.setLongitude(coordinate[0]);
            newCode.setLatitude(coordinate[1]);
            newCode.setCoordinateSystem(GeoCoordinateSystem.WGS84());
            return newCode;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Location> toWGS84(List<Location> locations) {
        if (locations == null) {
            return locations;
        }
        List<Location> newLocations = new ArrayList<>();
        for (Location location : locations) {
            if (!Location.valid(location)) {
                continue;
            }
            Location newLocation = toWGS84(location);
            if (newLocation != null) {
                newLocations.add(newLocation);
            }
        }
        return newLocations;
    }

    public static Location toGCJ02(Location location) {
        try {
            if (location == null || !Location.valid(location)
                    || location.getCoordinateSystem() == null) {
                return location;
            }
            GeoCoordinateSystem cs = location.getCoordinateSystem();
            double[] coordinate;
            switch (cs.getValue()) {
                case CGCS2000:
                case WGS_84:
                    coordinate = CoordinateConverter.WGS84ToGCJ02(location.getLongitude(), location.getLatitude());
                    break;
                case BD_09:
                    coordinate = CoordinateConverter.BD09ToGCJ02(location.getLongitude(), location.getLatitude());
                    break;
                case Mapbar:
                    coordinate = GeographyCodeTools.toGCJ02ByWebService(location.getCoordinateSystem(),
                            location.getLongitude(), location.getLatitude());
                    break;
                case GCJ_02:
                default:
                    return location;
            }
            Location newLocation = (Location) location.clone();
            newLocation.setLdid(-1);
            newLocation.setLongitude(coordinate[0]);
            newLocation.setLatitude(coordinate[1]);
            newLocation.setCoordinateSystem(GeoCoordinateSystem.GCJ02());
            return newLocation;
        } catch (Exception e) {
            return null;
        }
    }

    public static List<Location> toGCJ02(List<Location> locations) {
        if (locations == null) {
            return locations;
        }
        List<Location> newLocations = new ArrayList<>();
        for (Location location : locations) {
            if (!Location.valid(location)) {
                continue;
            }
            Location newLocation = toGCJ02(location);
            if (newLocation != null) {
                newLocations.add(newLocation);
            }
        }
        return newLocations;
    }

    public static List<Location> toGCJ02ByWebService(GeoCoordinateSystem sourceCS, List<Location> codes) {
        try {
            if (codes == null || codes.isEmpty()) {
                return null;
            }
            int size = codes.size();
            int batch = size % 40 == 0 ? size / 40 : size / 40 + 1;
            List<Location> newLocations = new ArrayList<>();
            for (int k = 0; k < batch; k++) {
                String locationsString = null;
                for (int i = k * 40; i < Math.min(size, k * 40 + 40); i++) {
                    Location code = codes.get(i);
                    if (locationsString == null) {
                        locationsString = "";
                    } else {
                        locationsString += "|";
                    }
                    locationsString += DoubleTools.scale(code.getLongitude(), 6) + "," + DoubleTools.scale(code.getLatitude(), 6);
                }
                String results = GeographyCodeTools.toGCJ02ByWebService(sourceCS, locationsString);
                String[] locationsValues = results.split(";");
                GeoCoordinateSystem GCJ02 = GeoCoordinateSystem.GCJ02();
                for (int i = 0; i < locationsValues.length; i++) {
                    String locationValue = locationsValues[i];
                    String[] values = locationValue.split(",");
                    double longitudeC = Double.parseDouble(values[0]);
                    double latitudeC = Double.parseDouble(values[1]);
                    Location newCode = (Location) codes.get(i).clone();
                    newCode.setLdid(-1);
                    newCode.setLongitude(longitudeC);
                    newCode.setLatitude(latitudeC);
                    newCode.setCoordinateSystem(GCJ02);
                    newLocations.add(newCode);
                }
            }
            return newLocations;
        } catch (Exception e) {
            return null;
        }
    }

}
