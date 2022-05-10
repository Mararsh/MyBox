package mara.mybox.db.data;

import java.io.File;
import java.util.Date;
import mara.mybox.data.CoordinateSystem;
import mara.mybox.data.Era;
import mara.mybox.db.DerbyBase;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxFileTools;
import mara.mybox.tools.DateTools;
import mara.mybox.value.AppValues;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-7-5
 * @License Apache License Version 2.0
 */
public class Location extends BaseData implements Cloneable {

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
        ldid = datasetid = -1;
        longitude = latitude = altitude = precision = speed = dataValue = dataSize = AppValues.InvalidDouble;
        startTime = endTime = duration = AppValues.InvalidLong;
        direction = AppValues.InvalidShort;
        coordinateSystem = CoordinateSystem.defaultCode();
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

    public static boolean setValue(Location data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "ldid":
                    data.setLdid(value == null ? -1 : (long) value);
                    return true;
                case "datasetid":
                    data.setDatasetid(value == null ? -1 : (long) value);
                    return true;
                case "label":
                    data.setLabel(value == null ? null : (String) value);
                    return true;
                case "address":
                    data.setAddress(value == null ? null : (String) value);
                    return true;
                case "longitude":
                    data.setLongitude(value == null ? AppValues.InvalidDouble : (Double) value);
                    return true;
                case "latitude":
                    data.setLatitude(value == null ? AppValues.InvalidDouble : (Double) value);
                    return true;
                case "altitude":
                    data.setAltitude(value == null ? AppValues.InvalidDouble : (Double) value);
                    return true;
                case "precision":
                    data.setPrecision(value == null ? AppValues.InvalidDouble : (Double) value);
                    return true;
                case "speed":
                    data.setSpeed(value == null ? AppValues.InvalidDouble : (Double) value);
                    return true;
                case "direction":
                    data.setDirection(value == null ? AppValues.InvalidShort : (short) value);
                    return true;
                case "coordinate_system":
                    data.setCoordinateSystem(value == null
                            ? CoordinateSystem.defaultCode() : new CoordinateSystem((short) value));
                    return true;
                case "data_value":
                    data.setDataValue(value == null ? AppValues.InvalidDouble : (Double) value);
                    return true;
                case "data_size":
                    data.setDataSize(value == null ? AppValues.InvalidDouble : (Double) value);
                    return true;
                case "start_time":
                    data.setStartTime(value == null ? AppValues.InvalidLong : (long) value);
                    return true;
                case "end_time":
                    data.setEndTime(value == null ? AppValues.InvalidLong : (long) value);
                    return true;
                case "location_image":
                    data.setImageName(value == null ? null : (String) value);
                    return true;
                case "location_comments":
                    data.setComments(value == null ? null : (String) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(Location data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "ldid":
                return data.getLdid();
            case "datasetid":
                return data.getDatasetid();
            case "label":
                return data.getLabel() == null ? null : (data.getLabel().length() > 2048 ? data.getLabel().substring(0, 2048) : data.getLabel());
            case "address":
                return data.getAddress() == null ? null : (data.getAddress().length() > 4096 ? data.getAddress().substring(0, 4096) : data.getAddress());
            case "longitude":
                return data.getLongitude();
            case "latitude":
                return data.getLatitude();
            case "altitude":
                return data.getAltitude();
            case "precision":
                return data.getPrecision();
            case "speed":
                return data.getSpeed();
            case "direction":
                return data.getDirection();
            case "coordinate_system":
                return data.getCoordinateSystem() == null
                        ? CoordinateSystem.defaultCode().shortValue()
                        : data.getCoordinateSystem().shortValue();
            case "data_value":
                return data.getDataValue();
            case "data_size":
                return data.getDataSize();
            case "start_time":
                return data.getStartTime();
            case "end_time":
                return data.getEndTime();
            case "location_image":
                return data.getImage() == null ? null : data.getImage().getAbsolutePath();
            case "location_comments":
                return data.getComments() == null ? null : (data.getComments().length() > 32672 ? data.getComments().substring(0, 32672) : data.getComments());
        }
        return null;
    }

    public static boolean valid(Location data) {
        return data != null
                && data.getLongitude() >= -180 && data.getLongitude() <= 180
                && data.getLatitude() >= -90 && data.getLatitude() <= 90;
    }

    public static String displayColumn(Location data, ColumnDefinition column, Object value) {
        if (data == null || column == null) {
            return null;
        }
        switch (column.getColumnName()) {
            case "datasetid":
                return data.getDatasetName();
            case "start_time":
                return DateTools.textEra(data.getStartEra());
            case "coordinate_system":
                CoordinateSystem cs = data.getCoordinateSystem();
                return cs != null ? cs.name() : null;
            case "end_time":
                return DateTools.textEra(data.getEndEra());
            case "location_image":
                if (data.getImage() != null) {
                    return data.getImage().getAbsolutePath();
                } else if (data.getDataset() != null && data.getDataset().getImage() != null) {
                    return data.getDataset().getImage().getAbsolutePath();
                }
        }
        return BaseDataAdaptor.displayColumnBase(data, column, value);
    }


    /*
        customized  get/set
     */
    public static void importChinaEarlyCultures() {
        File file;
        if ("zh".equals(Languages.getLanguage())) {
            file = FxFileTools.getInternalFile("/data/examples/Location_zh.del", "AppTemp", "Location_zh.del");
        } else {
            file = FxFileTools.getInternalFile("/data/examples/Location_en.del", "AppTemp", "Location_en.del");
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
        if (getStartTime() == AppValues.InvalidLong) {
            startTimeText = null;
        } else if (dataset == null) {
            startTimeText = DateTools.textEra(startTime);
        } else {
            startTimeText = DateTools.textEra(startTime, dataset.getTimeFormat(), dataset.isOmitAD());
        }
        return startTimeText;
    }

    public String getEndTimeText() {
        if (getEndTime() == AppValues.InvalidLong) {
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
        if (getStartTime() == AppValues.InvalidLong) {
            startEra = null;
        } else if (dataset == null) {
            startEra = new Era(startTime);
        } else {
            startEra = new Era(startTime, dataset.getTimeFormat(), dataset.isOmitAD());
        }
        return startEra;
    }

    public Era getEndEra() {
        if (getEndTime() == AppValues.InvalidLong) {
            endEra = null;
        } else if (dataset == null) {
            endEra = new Era(endTime);
        } else {
            endEra = new Era(endTime, dataset.getTimeFormat(), dataset.isOmitAD());
        }
        return endEra;
    }

    public long getDuration() {
        if (startTime != endTime && startTime != AppValues.InvalidLong && endTime != AppValues.InvalidLong) {
            duration = endTime - startTime;
        } else {
            duration = AppValues.InvalidLong;
        }
        return duration;
    }

    public String getDurationText() {
        if (startTime == endTime || startTime == AppValues.InvalidLong || endTime == AppValues.InvalidLong) {
            return null;
        }
        if (dataset != null) {
            return DateTools.duration(new Date(startTime), new Date(endTime), dataset.timeFormat);
        } else {
            return DateTools.duration(new Date(startTime), new Date(endTime), null);
        }
    }

    public Location setImageName(String string) {
        this.image = null;
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
