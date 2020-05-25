package mara.mybox.tools;

import javafx.scene.web.WebEngine;
import mara.mybox.data.GeographyCode;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-1-20
 * @License Apache License Version 2.0
 */
public class LocationTools {

    public static boolean validCoordinate(double longitude, double latitude) {
        return longitude >= -180 && longitude <= 180
                && latitude >= -90 && latitude <= 90;
    }

    public static boolean validCoordinate(GeographyCode code) {
        if (code == null) {
            return false;
        }
        return code.getLongitude() >= -180 && code.getLongitude() <= 180
                && code.getLatitude() >= -90 && code.getLatitude() <= 90;
    }

    public static void addMarkerInGaoDeMap(WebEngine webEngine,
            double longitude, double latitude, String label, String info,
            String image, boolean multiple, int mapSize, int markSize, int textSize) {
        String jsLabel = (label == null || label.trim().isBlank()
                ? "null" : "'" + label.replaceAll("'", CommonValues.MyBoxSeparator) + "'");
        String jsInfo = (info == null || info.trim().isBlank()
                ? "null" : "'" + info.replaceAll("'", CommonValues.MyBoxSeparator) + "'");
        String jsImage = (image == null || image.trim().isBlank()
                ? "null" : "'" + StringTools.replaceAll(image, "\\", "/") + "'");
        webEngine.executeScript("addMarker("
                + longitude + "," + latitude
                + ", " + jsLabel + ", " + jsInfo + ", " + jsImage
                + ", " + multiple + ", " + mapSize + ", " + markSize + ", " + textSize + ");");
    }

}
