package mara.mybox.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;
import java.util.List;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.TableBase;
import mara.mybox.db.TableLocationData;
import mara.mybox.fxml.FxmlControl;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2020-7-5
 * @License Apache License Version 2.0
 */
public class Location extends TableData implements Cloneable {

    protected long ldid, datasetid, startTime, endTime, duration;
    protected String datasetName, address, comments, label,
            startTimeText, endTimeText, periodText, durationText;
    protected double longitude, latitude, altitude, precision, speed, dataValue, dataSize;
    protected short direction;
    protected File image;
    protected CoordinateSystem coordinateSystem;
    protected Dataset dataset;
    protected Era startEra, endEra;

    public Location() {
        init();
    }

    private void init() {
        ldid = datasetid = -1;
        longitude = latitude = altitude = precision = speed = dataValue = dataSize = CommonValues.InvalidDouble;
        startTime = endTime = duration = CommonValues.InvalidLong;
        direction = CommonValues.InvalidShort;
        coordinateSystem = CoordinateSystem.defaultCode();
    }

    public boolean validCoordinate() {
        return longitude >= -180 && longitude <= 180
                && latitude >= -90 && latitude <= 90;
    }

    @Override
    public TableBase getTable() {
        if (table == null) {
            table = new TableLocationData();
        }
        return table;
    }

    @Override
    public boolean valid() {
        return validCoordinate();
    }

    @Override
    public boolean setValue(String column, Object value) {
        if (column == null) {
            return false;
        }
        try {
            switch (column) {
                case "ldid":
                    ldid = value == null ? -1 : (long) value;
                    return true;
                case "datasetid":
                    datasetid = value == null ? -1 : (long) value;
                    return true;
                case "label":
                    label = value == null ? null : (String) value;
                    return true;
                case "address":
                    address = value == null ? null : (String) value;
                    return true;
                case "longitude":
                    longitude = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "latitude":
                    latitude = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "altitude":
                    altitude = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "precision":
                    precision = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "speed":
                    speed = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "direction":
                    direction = value == null ? CommonValues.InvalidShort : (short) value;
                    return true;
                case "coordinate_system":
                    coordinateSystem = value == null
                            ? CoordinateSystem.defaultCode() : new CoordinateSystem((short) value);
                    return true;
                case "data_value":
                    dataValue = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "data_size":
                    dataSize = value == null ? CommonValues.InvalidDouble : (Double) value;
                    return true;
                case "start_time":
                    startTime = value == null ? CommonValues.InvalidLong : (long) value;
                    return true;
                case "end_time":
                    endTime = value == null ? CommonValues.InvalidLong : (long) value;
                    return true;
                case "location_image":
                    image = null;
                    if (value != null) {
                        File file = new File((String) value);
                        if (file.exists()) {
                            image = file;
                        }
                    }
                    return true;
                case "location_comments":
                    comments = value == null ? null : (String) value;
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    @Override
    public Object getValue(String column) {
        if (column == null) {
            return null;
        }
        switch (column) {
            case "ldid":
                return ldid;
            case "datasetid":
                return this.getDatasetid();
            case "label":
                return label == null ? null : (label.length() > 2048 ? label.substring(0, 2048) : label);
            case "address":
                return address == null ? null : (address.length() > 4096 ? address.substring(0, 4096) : address);
            case "longitude":
                return longitude;
            case "latitude":
                return latitude;
            case "altitude":
                return altitude;
            case "precision":
                return precision;
            case "speed":
                return speed;
            case "direction":
                return direction;
            case "coordinate_system":
                return coordinateSystem == null
                        ? CoordinateSystem.defaultCode().intValue()
                        : coordinateSystem.intValue();
            case "data_value":
                return dataValue;
            case "data_size":
                return dataSize;
            case "start_time":
                return this.getStartTime();
            case "end_time":
                return this.getEndTime();
            case "location_image":
                return image == null ? null : image.getAbsolutePath();
            case "location_comments":
                return comments == null ? null : (comments.length() > 32672 ? comments.substring(0, 32672) : comments);
        }
        return null;
    }

    public String info(String lineBreak) {
        StringBuilder s = new StringBuilder();
        if (getDatasetName() != null) {
            s.append(message("Dataset")).append(": ").append(datasetName).append(lineBreak);
        }
        if (label != null) {
            s.append(message("Label")).append(": ").append(label).append(lineBreak);
        }
        if (address != null) {
            s.append(message("Address")).append(": ").append(address).append(lineBreak);
        }
        if (longitude >= -180 && longitude <= 180) {
            s.append(message("Longitude")).append(": ").append(longitude).append(lineBreak);
        }
        if (latitude >= -90 && latitude <= 90) {
            s.append(message("Latitude")).append(": ").append(latitude).append(lineBreak);
        }
        if (altitude != CommonValues.InvalidDouble) {
            s.append(message("Altitude")).append(": ").append(altitude).append(lineBreak);
        }
        if (precision != CommonValues.InvalidDouble) {
            s.append(message("Precision")).append(": ").append(precision).append(lineBreak);
        }
        if (speed != CommonValues.InvalidDouble) {
            s.append(message("Speed")).append(": ").append(speed).append(lineBreak);
        }
        if (direction != CommonValues.InvalidShort) {
            s.append(message("Direction")).append(": ").append(direction).append(lineBreak);
        }
        if (coordinateSystem != null) {
            s.append(message("CoordinateSystem")).append(": ").append(coordinateSystem.name()).append(lineBreak);
        }
        if (dataValue != CommonValues.InvalidDouble) {
            s.append(message("DataValue")).append(": ").append(dataValue).append(lineBreak);
        }
        if (dataSize != CommonValues.InvalidDouble) {
            s.append(message("DataSize")).append(": ").append(dataSize).append(lineBreak);
        }
        if (startTime != CommonValues.InvalidLong) {
            String t;
            if (dataset != null) {
                t = DateTools.textEra(getStartEra());
            } else {
                t = DateTools.textEra(startTime);
            }
            s.append(message("StartTime")).append(": ").append(t).append(lineBreak);
        }
        if (endTime != CommonValues.InvalidLong) {
            String t;
            if (dataset != null) {
                t = DateTools.textEra(getEndEra());
            } else {
                t = DateTools.textEra(endTime);
            }
            s.append(message("EndTime")).append(": ").append(t).append(lineBreak);
        }
        String imageFile = null;
        if (image != null) {
            imageFile = image.getAbsolutePath();
        } else if (dataset != null && dataset.getImage() != null) {
            imageFile = dataset.getImage().getAbsolutePath();
        }
        if (imageFile != null) {
            if (lineBreak.toLowerCase().equals("</br>")) {
                s.append(message("Image")).append(": ").
                        append("<img src=\"file:///").append(imageFile.replaceAll("\\\\", "/"))
                        .append("\" width=200px>").append(lineBreak);
            } else {
                s.append(message("Image")).append(": ").append(imageFile).append(lineBreak);
            }
        }
        if (comments != null) {
            s.append(message("Comments")).append(": ").append(comments).append(lineBreak);
        }
        return s.toString();
    }

    @Override
    public Object clone() throws CloneNotSupportedException {
        try {
            Location newCode = (Location) super.clone();
            if (coordinateSystem != null) {
                newCode.setCoordinateSystem((CoordinateSystem) coordinateSystem.clone());
            }
            if (dataset != null) {
                newCode.setDataset((Dataset) dataset.clone());
            }
            return newCode;
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
            return null;
        }
    }

    /*
        static methods
     */
    public static Location create() {
        return new Location();
    }

    public static Location create(double longitude, double latitude) {
        return new Location().setLongitude(longitude).setLatitude(latitude);
    }

    public static Location create(GeographyCode code) {
        return new Location()
                .setLongitude(code.getLongitude())
                .setLatitude(code.getLatitude())
                .setAltitude(code.getAltitude())
                .setPrecision(code.getPrecision())
                .setCoordinateSystem(code.getCoordinateSystem())
                .setLabel(code.getName())
                .setAddress(code.getFullName())
                .setComments(code.getComments());
    }

    public static List<String> externalValues(Location data) {
        List<String> row = new ArrayList<>();
        row.addAll(Arrays.asList(
                data.getDatasetName() == null ? "" : data.getDatasetName(),
                data.getLabel() == null ? "" : data.getLabel(),
                data.getAddress() == null ? "" : data.getAddress(),
                data.getLongitude() >= -180 && data.getLongitude() <= 180 ? data.getLongitude() + "" : "",
                data.getLatitude() >= -90 && data.getLatitude() <= 90 ? data.getLatitude() + "" : "",
                data.getAltitude() != CommonValues.InvalidDouble ? data.getAltitude() + "" : "",
                data.getPrecision() != CommonValues.InvalidDouble ? data.getPrecision() + "" : "",
                data.getSpeed() != CommonValues.InvalidDouble ? data.getSpeed() + "" : "",
                data.getDirection() != CommonValues.InvalidInteger ? data.getDirection() + "" : "",
                data.getCoordinateSystem() == null ? "" : data.getCoordinateSystem().name(),
                data.getDataValue() != CommonValues.InvalidDouble ? data.getDataValue() + "" : "",
                data.getDataSize() != CommonValues.InvalidDouble ? data.getDataSize() + "" : "",
                data.getStartTime() != CommonValues.InvalidLong ? data.getStartTimeText() : "",
                data.getEndTime() != CommonValues.InvalidLong ? data.getEndTimeText() : "",
                data.getImage() == null ? "" : data.getImage().getAbsolutePath(),
                data.getComments() == null ? "" : data.getComments()
        ));
        return row;
    }

    public static List<String> externalValues(List<String> columns, Location data) {
        List<String> row = new ArrayList<>();
        if (columns == null || columns.isEmpty()) {
            return row;
        }
        String lang = columns.contains(message("zh", "Longitude")) ? "zh" : "en";
        if (columns.contains(message(lang, "Dataset"))) {
            row.add(data.getDatasetName() == null ? "" : data.getDatasetName());
        }
        if (columns.contains(message(lang, "Label"))) {
            row.add(data.getLabel() == null ? "" : data.getLabel());
        }
        if (columns.contains(message(lang, "Address"))) {
            row.add(data.getAddress() == null ? "" : data.getAddress());
        }
        if (columns.contains(message(lang, "Longitude"))) {
            row.add(data.getLongitude() >= -180 && data.getLongitude() <= 180 ? data.getLongitude() + "" : "");
        }
        if (columns.contains(message(lang, "Latitude"))) {
            row.add(data.getLatitude() >= -90 && data.getLatitude() <= 90 ? data.getLatitude() + "" : "");
        }
        if (columns.contains(message(lang, "Altitude"))) {
            row.add(data.getAltitude() != CommonValues.InvalidDouble ? data.getAltitude() + "" : "");
        }
        if (columns.contains(message(lang, "Precision"))) {
            row.add(data.getPrecision() != CommonValues.InvalidDouble ? data.getPrecision() + "" : "");
        }
        if (columns.contains(message(lang, "CoordinateSystem"))) {
            row.add(data.getCoordinateSystem() == null ? "" : data.getCoordinateSystem().name());
        }
        if (columns.contains(message(lang, "Speed"))) {
            row.add(data.getSpeed() != CommonValues.InvalidDouble ? data.getSpeed() + "" : "");
        }
        if (columns.contains(message(lang, "Direction"))) {
            row.add(data.getDirection() != CommonValues.InvalidShort ? data.getDirection() + "" : "");
        }
        if (columns.contains(message(lang, "DataValue"))) {
            row.add(data.getDataValue() != CommonValues.InvalidDouble ? data.getDataValue() + "" : "");
        }
        if (columns.contains(message(lang, "DataSize"))) {
            row.add(data.getDataSize() != CommonValues.InvalidDouble ? data.getDataSize() + "" : "");
        }
        if (columns.contains(message(lang, "StartTime"))) {
            row.add(data.getStartTime() != CommonValues.InvalidLong ? data.getStartTimeText() : "");
        }
        if (columns.contains(message(lang, "EndTime"))) {
            row.add(data.getEndTime() != CommonValues.InvalidLong ? data.getEndTimeText() : "");
        }
        if (columns.contains(message(lang, "Image"))) {
            row.add(data.getImage() == null ? "" : data.getImage().getAbsolutePath());
        }
        if (columns.contains(message(lang, "Comments"))) {
            row.add(data.getComments() == null ? "" : data.getComments());
        }
        return row;
    }

    /*
        customized  get/set
     */
    public static void importChinaEarlyCultures() {
        File file;
        if ("zh".equals(AppVariables.getLanguage())) {
            file = FxmlControl.getInternalFile("/data/db/Location_zh.del",
                    "AppTemp", "Location_zh.del");
        } else {
            file = FxmlControl.getInternalFile("/data/db/Location_en.del",
                    "AppTemp", "Location_en.del");
        }
        DerbyBase.importData("Location", file.getAbsolutePath(), false);
    }

    /*
        custmized get/set
     */
    public long getDatasetid() {
        if (dataset != null) {
            datasetid = dataset.getDsid();
        }
        return datasetid;
    }

    public Location setDataset(Dataset dataset) {
        this.dataset = dataset;
        if (dataset != null) {
            datasetid = dataset.getDsid();
        }
        return this;
    }

    public String getDatasetName() {
        if (dataset != null) {
            datasetName = dataset.getDataSet();
        }
        return datasetName;
    }

    public long getStartTime() {
//        if (startTime == CommonValues.InvalidLong && endTime != CommonValues.InvalidLong) {
//            startTime = endTime;
//        }
        return startTime;
    }

    public long getEndTime() {
//        if (endTime == CommonValues.InvalidLong && startTime != CommonValues.InvalidLong) {
//            endTime = startTime;
//        }
        return endTime;
    }

    public String getStartTimeText() {
        if (getStartTime() == CommonValues.InvalidLong) {
            startTimeText = null;
        } else if (dataset == null) {
            startTimeText = DateTools.textEra(startTime);
        } else {
            startTimeText = DateTools.textEra(startTime, dataset.getTimeFormat(), dataset.isOmitAD());
        }
        return startTimeText;
    }

    public String getEndTimeText() {
        if (getEndTime() == CommonValues.InvalidLong) {
            endTimeText = null;
        } else if (dataset == null) {
            endTimeText = DateTools.textEra(endTime);
        } else {
            endTimeText = DateTools.textEra(endTime, dataset.getTimeFormat(), dataset.isOmitAD());
        }
        return endTimeText;
    }

    public String getPeriodText() {
        String startText = getStartTimeText();
        String endText = getEndTimeText();
        if (startText != null) {
            if (endText != null) {
                if (!startText.equals(endText)) {
                    return startText + " - " + endText;
                } else {
                    return startText;
                }
            } else {
                return startText + " - ";
            }
        } else {
            if (endText != null) {
                return " - " + endText;
            } else {
                return null;
            }
        }
    }

    public Era getStartEra() {
        if (getStartTime() == CommonValues.InvalidLong) {
            startEra = null;
        } else if (dataset == null) {
            startEra = new Era(startTime);
        } else {
            startEra = new Era(startTime, dataset.getTimeFormat(), dataset.isOmitAD());
        }
        return startEra;
    }

    public Era getEndEra() {
        if (getEndTime() == CommonValues.InvalidLong) {
            endEra = null;
        } else if (dataset == null) {
            endEra = new Era(endTime);
        } else {
            endEra = new Era(endTime, dataset.getTimeFormat(), dataset.isOmitAD());
        }
        return endEra;
    }

    public long getDuration() {
        if (startTime != endTime && startTime != CommonValues.InvalidLong && endTime != CommonValues.InvalidLong) {
            duration = endTime - startTime;
        } else {
            duration = CommonValues.InvalidLong;
        }
        return duration;
    }

    public String getDurationText() {
        if (startTime == endTime || startTime == CommonValues.InvalidLong || endTime == CommonValues.InvalidLong) {
            return null;
        }
        if (dataset != null) {
            return DateTools.duration(new Date(startTime), new Date(endTime), dataset.timeFormat);
        } else {
            return DateTools.duration(new Date(startTime), new Date(endTime), null);
        }
    }

    public Location setImage(String string) {
        if (string != null) {
            File file = new File(string);
            if (file.exists()) {
                this.image = file;
            }
        }
        return this;
    }

    /*
        get/set
     */
    public long getLdid() {
        return ldid;
    }

    public void setLdid(long ldid) {
        this.ldid = ldid;
    }

    public double getLongitude() {
        return longitude;
    }

    public Location setLongitude(double longitude) {
        this.longitude = longitude;
        return this;
    }

    public double getLatitude() {
        return latitude;
    }

    public Location setLatitude(double latitude) {
        this.latitude = latitude;
        return this;
    }

    public double getAltitude() {
        return altitude;
    }

    public Location setAltitude(double altitude) {
        this.altitude = altitude;
        return this;
    }

    public double getPrecision() {
        return precision;
    }

    public Location setPrecision(double precision) {
        this.precision = precision;
        return this;
    }

    public CoordinateSystem getCoordinateSystem() {
        return coordinateSystem;
    }

    public Location setCoordinateSystem(CoordinateSystem coordinateSystem) {
        this.coordinateSystem = coordinateSystem;
        return this;
    }

    public Dataset getDataset() {
        return dataset;
    }

    public Location setDatasetid(long datasetid) {
        this.datasetid = datasetid;
        return this;
    }

    public Location setDatasetName(String datasetName) {
        this.datasetName = datasetName;
        return this;
    }

    public String getAddress() {
        return address;
    }

    public Location setAddress(String address) {
        this.address = address;
        return this;
    }

    public String getComments() {
        return comments;
    }

    public Location setComments(String comments) {
        this.comments = comments;
        return this;
    }

    public File getImage() {
        return image;
    }

    public Location setImage(File image) {
        this.image = image;
        return this;
    }

    public String getLabel() {
        return label;
    }

    public Location setLabel(String label) {
        this.label = label;
        return this;
    }

    public double getSpeed() {
        return speed;
    }

    public Location setSpeed(double speed) {
        this.speed = speed;
        return this;
    }

    public double getDataValue() {
        return dataValue;
    }

    public Location setDataValue(double dataValue) {
        this.dataValue = dataValue;
        return this;
    }

    public double getDataSize() {
        return dataSize;
    }

    public Location setDataSize(double dataSize) {
        this.dataSize = dataSize;
        return this;
    }

    public short getDirection() {
        return direction;
    }

    public Location setDirection(short direction) {
        this.direction = direction;
        return this;
    }

    public Location setStartTime(long startTime) {
        this.startTime = startTime;
        return this;
    }

    public Location setEndTime(long endTime) {
        this.endTime = endTime;
        return this;
    }

    public Location setStartEra(Era startEra) {
        this.startEra = startEra;
        return this;
    }

    public Location setEndEra(Era endEra) {
        this.endEra = endEra;
        return this;
    }

    public void setDuration(long duration) {
        this.duration = duration;
    }

    public void setStartTimeText(String startTimeText) {
        this.startTimeText = startTimeText;
    }

    public void setEndTimeText(String endTimeText) {
        this.endTimeText = endTimeText;
    }

    public void setPeriodText(String periodText) {
        this.periodText = periodText;
    }

    public void setDurationText(String durationText) {
        this.durationText = durationText;
    }

}
