package mara.mybox.db.data;

import java.util.Date;

/**
 * @Author Mara
 * @CreateDate 2019-4-5
 * @Version 1.0
 * @Description
 * @License Apache License Version 2.0
 */
public class VisitHistory {

    private int resourceType, fileType, operationType;
    private String resourceValue, dataMore;
    private Date lastVisitTime;
    private int visitCount;

    public VisitHistory() {

    }

    public VisitHistory(String value) {
        resourceValue = value;
    }

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
        public static int Xml = 13;
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
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return types;

        } else if (fileType == FileType.Media) {
            int[] types = {FileType.Media, FileType.Video, FileType.Audio};
            return types;

        } else if (fileType == FileType.Sheet) {
            int[] types = {FileType.Sheet, FileType.Excel, FileType.CSV};
            return types;

        } else {
            return null;
        }

    }

    /*
        get/set
     */
    public int getResourceType() {
        return resourceType;
    }

    public void setResourceType(int resourceType) {
        this.resourceType = resourceType;
    }

    public int getFileType() {
        return fileType;
    }

    public void setFileType(int fileType) {
        this.fileType = fileType;
    }

    public int getOperationType() {
        return operationType;
    }

    public void setOperationType(int operationType) {
        this.operationType = operationType;
    }

    public String getResourceValue() {
        return resourceValue;
    }

    public void setResourceValue(String resourceValue) {
        this.resourceValue = resourceValue;
    }

    public String getDataMore() {
        return dataMore;
    }

    public void setDataMore(String dataMore) {
        this.dataMore = dataMore;
    }

    public Date getLastVisitTime() {
        return lastVisitTime;
    }

    public void setLastVisitTime(Date lastVisitTime) {
        this.lastVisitTime = lastVisitTime;
    }

    public int getVisitCount() {
        return visitCount;
    }

    public void setVisitCount(int visitCount) {
        this.visitCount = visitCount;
    }

}
