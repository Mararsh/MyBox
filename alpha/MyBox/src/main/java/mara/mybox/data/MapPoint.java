package mara.mybox.data;

import java.util.ArrayList;
import java.util.List;
import javafx.scene.paint.Color;
import mara.mybox.db.data.GeographyCode;
import mara.mybox.db.data.GeographyCodeTools;

/**
 * @Author Mara
 * @CreateDate 2022-10-23
 * @License Apache License Version 2.0
 */
public class MapPoint {

    protected double longitude, latitude;
    protected String label, info, markerImage;
    protected int markSize, textSize;
    protected boolean isBold;
    protected Color textColor;
    protected GeoCoordinateSystem cs;

    public MapPoint() {
        init();
    }

    public final void init() {
        markSize = 24;
        textSize = 12;
        textColor = Color.BLACK;
        cs = GeoCoordinateSystem.defaultCode();
        isBold = false;
    }

    public MapPoint(double longitude, double latitude, String label, String info) {
        init();
        this.longitude = longitude;
        this.latitude = latitude;
        this.label = label;
        this.info = info;
    }

    public MapPoint(GeographyCode code) {
        init();
        this.longitude = code.getLongitude();
        this.latitude = code.getLatitude();
        this.label = code.getLabel();
        this.info = code.getInfo();
        this.cs = code.getCoordinateSystem();
    }

    public boolean valid() {
        return GeographyCodeTools.validCoordinate(longitude, latitude);
    }

    public List<String> dataValues() {
        List<String> row = new ArrayList<>();
        row.add(longitude + "");
        row.add(latitude + "");
        row.add(label == null ? null : label.replaceAll("<BR>", "\n"));
        row.add(info == null ? null : info.replaceAll("<BR>", "\n"));
        return row;
    }

    public List<String> htmlValues() {
        List<String> row = new ArrayList<>();
        row.add(longitude + "");
        row.add(latitude + "");
        row.add(label == null ? null : label.replaceAll("\n", "<BR>"));
        row.add(info == null ? null : info.replaceAll("\n", "<BR>"));
        return row;
    }

    /*
        set/get
     */
    public double getLongitude() {
        return longitude;
    }

    public MapPoint setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public MapPoint setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public MapPoint setLabel(String label) {
        this.label = label;
        return this;
    }

    public String getInfo() {
        return info;
    }

    public MapPoint setInfo(String info) {
        this.info = info;
        return this;
    }

    public String getMarkerImage() {
        return markerImage;
    }

    public MapPoint setMarkerImage(String markerImage) {
        this.markerImage = markerImage;
        return this;
    }

    public int getMarkSize() {
        return markSize;
    }

    public MapPoint setMarkSize(int markSize) {
        this.markSize = markSize;
        return this;
    }

    public Color getTextColor() {
        return textColor;
    }

    public MapPoint setTextColor(Color textColor) {
        this.textColor = textColor;
        return this;
    }

    public int getTextSize() {
        return textSize;
    }

    public MapPoint setTextSize(int textSize) {
        this.textSize = textSize;
        return this;
    }

    public boolean isIsBold() {
        return isBold;
    }

    public MapPoint setIsBold(boolean isBold) {
        this.isBold = isBold;
        return this;
    }

    public GeoCoordinateSystem getCs() {
        return cs;
    }

    public MapPoint setCs(GeoCoordinateSystem cs) {
        this.cs = cs;
        return this;
    }

}
