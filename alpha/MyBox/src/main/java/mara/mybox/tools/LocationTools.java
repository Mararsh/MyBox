package mara.mybox.tools;

import java.io.File;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationTools {

    public static File tiandituFile(boolean geodetic) {
        try {
            File map = FxFileTools.getInternalFile("/js/tianditu.html", "js", "tianditu.html");
            String html = TextFileTools.readTexts(null, map);
            html = html.replace(AppValues.TianDiTuWebKey,
                    UserConfig.getString("TianDiTuWebKey", AppValues.TianDiTuWebKey));
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
            String html = TextFileTools.readTexts(null, map);
            html = html.replace(AppValues.GaoDeMapJavascriptKey,
                    UserConfig.getString("GaoDeMapWebKey", AppValues.GaoDeMapJavascriptKey));
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

}
