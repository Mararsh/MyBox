package mara.mybox.db.data;

import java.util.Date;
import mara.mybox.dev.MyBoxLog;

/**
 * @Author Mara
 * @CreateDate 2019-4-5
 * @License Apache License Version 2.0
 */
public class VisitHistory extends BaseData {

    public static final int Default_Max_Histories = 12;

    private short resourceType, fileType, operationType;
    private String resourceValue, dataMore;
    private Date lastVisitTime;
    private int visitCount;

    public static class ResourceType {

        public static int Path = 1;
        public static int File = 2;
        public static int URI = 3;
        public static int Menu = 4;

        public static int None = 100;
    }

    public static class FileType {

        public static int All = 0;
        public static int General = 1;
        public static int Image = 2;
        public static int PDF = 3;
        public static int Text = 4;
        public static int Bytes = 5;
        public static int Gif = 6;
        public static int Tif = 7;
        public static int MultipleFrames = 8;
        public static int Audio = 9;
        public static int Video = 10;
        public static int Html = 11;
        public static int Icc = 12;
        public static int XML = 13;
        public static int Markdown = 14;
        public static int Media = 15;
        public static int Certificate = 16;
        public static int StreamMedia = 17;
        public static int TTC = 18;
        public static int TTF = 19;
        public static int Excel = 20;
        public static int CSV = 21;
        public static int Sheet = 22;
        public static int Cert = 23;
        public static int Word = 24;
        public static int PPT = 25;
        public static int PPTX = 26;
        public static int PPTS = 27;
        public static int WordX = 28;
        public static int WordS = 29;
        public static int ImagesList = 30;
        public static int Jar = 31;
        public static int DataFile = 32;
        public static int JSON = 33;

        public static int None = 100;
    }

    public static class OperationType {

        public static int Access = 1;
        public static int Read = 2;
        public static int Write = 3;
        public static int Alpha = 4;
        public static int Download = 5;

        public static int None = 100;
    }

    public static int[] typeGroup(int fileType) {
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return types;

        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif,
                FileType.MultipleFrames};
            return types;

        } else if (fileType == FileType.Media) {
            int[] types = {FileType.Media, FileType.Video, FileType.Audio};
            return types;

        } else if (fileType == FileType.Sheet) {
            int[] types = {FileType.Sheet, FileType.Excel, FileType.CSV};
            return types;

        } else if (fileType == FileType.ImagesList) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif,
                FileType.MultipleFrames, FileType.PDF, FileType.PPT};
            return types;

        } else {
            return null;
        }

    }

    public VisitHistory() {
    }

    public VisitHistory(String value) {
        resourceValue = value;
    }

    /*
        Static methods
     */
    public static VisitHistory create() {
        return new VisitHistory();
    }

    public static boolean setValue(VisitHistory data, String column, Object value) {
        if (data == null || column == null) {
            return false;
        }
        try {
            switch (column) {
                case "resource_type":
                    data.setResourceType(value == null ? null : (short) value);
                    return true;
                case "file_type":
                    data.setFileType(value == null ? null : (short) value);
                    return true;
                case "operation_type":
                    data.setOperationType(value == null ? null : (short) value);
                    return true;
                case "resource_value":
                    data.setResourceValue(value == null ? null : (String) value);
                    return true;
                case "last_visit_time":
                    data.setLastVisitTime(value == null ? null : (Date) value);
                    return true;
                case "data_more":
                    data.setDataMore(value == null ? null : (String) value);
                    return true;
                case "visit_count":
                    data.setVisitCount(value == null ? null : (int) value);
                    return true;
            }
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        return false;
    }

    public static Object getValue(VisitHistory data, String column) {
        if (data == null || column == null) {
            return null;
        }
        switch (column) {
            case "resource_type":
                return data.getResourceType();
            case "file_type":
                return data.getFileType();
            case "operation_type":
                return data.getOperationType();
            case "resource_value":
                return data.getResourceValue();
            case "last_visit_time":
                return data.getLastVisitTime();
            case "data_more":
                return data.getDataMore();
            case "visit_count":
                return data.getVisitCount();
        }
        return null;
    }

    public static boolean valid(VisitHistory data) {
        return data != null;
    }

    /*
        get/set
     */
    public short getResourceType() {
        return resourceType;
    }

    public VisitHistory setResourceType(short resourceType) {
        this.resourceType = resourceType;
        return this;
    }

    public short getFileType() {
        return fileType;
    }

    public VisitHistory setFileType(short fileType) {
        this.fileType = fileType;
        return this;
    }

    public short getOperationType() {
        return operationType;
    }

    public VisitHistory setOperationType(short operationType) {
        this.operationType = operationType;
        return this;
    }

    public String getResourceValue() {
        return resourceValue;
    }

    public VisitHistory setResourceValue(String resourceValue) {
        this.resourceValue = resourceValue;
        return this;
    }

    public String getDataMore() {
        return dataMore;
    }

    public VisitHistory setDataMore(String dataMore) {
        this.dataMore = dataMore;
        return this;
    }

    public Date getLastVisitTime() {
        return lastVisitTime;
    }

    public VisitHistory setLastVisitTime(Date lastVisitTime) {
        this.lastVisitTime = lastVisitTime;
        return this;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public VisitHistory setVisitCount(int visitCount) {
        this.visitCount = visitCount;
        return this;
    }

}
