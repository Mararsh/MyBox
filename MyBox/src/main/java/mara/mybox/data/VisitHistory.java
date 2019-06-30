package mara.mybox.data;

import java.io.File;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import mara.mybox.db.TableVisitHistory;
import mara.mybox.value.AppVaribles;

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

    public static class ResourceType {

        public static int Path = 1;
        public static int File = 2;
        public static int URI = 3;
        public static int Menu = 4;

        public static int None = 100;
    }

    public static class FileType {

        public static int General = 1;
        public static int Image = 2;
        public static int PDF = 3;
        public static int Text = 4;
        public static int Bytes = 5;
        public static int Gif = 6;
        public static int Tif = 7;
        public static int MultipleFrames = 8;
        public static int Sound = 9;
        public static int Video = 10;
        public static int Html = 11;
        public static int Icc = 12;
        public static int Xml = 13;

        public static int None = 100;
    }

    public static class OperationType {

        public static int Access = 1;
        public static int Read = 2;
        public static int Write = 3;
        public static int Alpha = 4;

        public static int None = 100;
    }

    public static List<VisitHistory> getRecentMenu() {
        return TableVisitHistory.find(ResourceType.Menu, 15);
    }

    public static boolean visitMenu(String name, String fxml) {
        return TableVisitHistory.update(ResourceType.Menu, FileType.General,
                OperationType.Access, name, fxml);
    }

    public static List<VisitHistory> getPath(int fileType) {
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.Path, types, 0);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.Path, types, 0);
        } else {
            return TableVisitHistory.find(ResourceType.Path, fileType, 0);
        }
    }

    public static List<VisitHistory> getPathRead(int fileType) {
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Read, 0);
    }

    public static List<VisitHistory> getPathWritten(int fileType) {
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Write, 0);
    }

    public static List<VisitHistory> getRecentPath(int fileType) {
        return getRecentPath(fileType, AppVaribles.fileRecentNumber);
    }

    public static List<VisitHistory> getRecentPath(int fileType, int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records;
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            records = TableVisitHistory.find(ResourceType.Path, types, number);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            records = TableVisitHistory.find(ResourceType.Path, types, number);
        } else {
            records = TableVisitHistory.find(ResourceType.Path, fileType, number);
        }
        return checkFilesExisted(records);
    }

    public static List<VisitHistory> getRecentPathRead(int fileType) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return null;
        }
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Read, AppVaribles.fileRecentNumber);
    }

    public static List<VisitHistory> getRecentPathWritten(int fileType) {
        if (AppVaribles.fileRecentNumber <= 0) {
            return null;
        }
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Write, AppVaribles.fileRecentNumber);
    }

    public static List<VisitHistory> getLastPath(int fileType) {
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.Path, types, 1);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.Path, types, 1);
        } else {
            return TableVisitHistory.find(ResourceType.Path, fileType, 1);
        }
    }

    public static List<VisitHistory> getLastPathRead(int fileType) {
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Read, 1);
    }

    public static List<VisitHistory> getLastPathWritten(int fileType) {
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Write, 1);
    }

    public static List<VisitHistory> getFile(int fileType) {
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.File, types, 0);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.File, types, 0);
        } else {
            return TableVisitHistory.find(ResourceType.File, fileType, 0);
        }
    }

    public static List<VisitHistory> getFileRead(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Read, 0);
    }

    public static List<VisitHistory> getFileWritten(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Write, 0);
    }

    public static List<VisitHistory> getRecentFile(int fileType) {
        return getRecentFile(fileType, AppVaribles.fileRecentNumber);
    }

    public static List<VisitHistory> getRecentFile(int fileType, int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records;
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            records = TableVisitHistory.find(ResourceType.File, types, number);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            records = TableVisitHistory.find(ResourceType.File, types, number);
        } else {
            records = TableVisitHistory.find(ResourceType.File, fileType, number);
        }
        return checkFilesExisted(records);
    }

    public static List<VisitHistory> getRecentAlphaImages(int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records = TableVisitHistory.findAlphaImages(number);
        return checkFilesExisted(records);
    }

    public static List<VisitHistory> checkFilesExisted(List<VisitHistory> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }
        List<VisitHistory> valid = new ArrayList();
        List<String> names = new ArrayList();
        for (VisitHistory r : records) {
            String fname = r.getResourceValue();
            if (!new File(fname).exists()) {
                TableVisitHistory.delete(r);
            } else if (!names.contains(fname)) {
                names.add(fname);
                valid.add(r);
            }
        }
        return valid;
    }

    public static List<VisitHistory> getRecentFileRead(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Read, AppVaribles.fileRecentNumber);
    }

    public static List<VisitHistory> getRecentFileWritten(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Write, AppVaribles.fileRecentNumber);
    }

    public static List<VisitHistory> getLastFile(int fileType) {
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.File, types, 1);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.File, types, 1);
        } else {
            return TableVisitHistory.find(ResourceType.File, fileType, 1);
        }
    }

    public static List<VisitHistory> getLastFileRead(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Read, 1);
    }

    public static List<VisitHistory> getLastFileWritten(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Write, 1);
    }

    public static boolean visitPath(int fileType, String value) {
        if (fileType == FileType.MultipleFrames) {
            return TableVisitHistory.update(ResourceType.Path, FileType.Image, OperationType.Access, value);
        } else {
            return TableVisitHistory.update(ResourceType.Path, fileType, OperationType.Access, value);
        }

    }

    public static boolean readPath(int fileType, String value) {

        if (fileType == FileType.MultipleFrames) {
            return TableVisitHistory.update(ResourceType.Path, FileType.Image, OperationType.Read, value);
        } else {
            return TableVisitHistory.update(ResourceType.Path, fileType, OperationType.Read, value);
        }

    }

    public static boolean writePath(int fileType, String value) {

        if (fileType == FileType.MultipleFrames) {
            return TableVisitHistory.update(ResourceType.Path, FileType.Image, OperationType.Write, value);
        } else {
            return TableVisitHistory.update(ResourceType.Path, fileType, OperationType.Write, value);
        }

    }

    public static boolean visitFile(int fileType, String value) {

        if (fileType == FileType.MultipleFrames || fileType == FileType.Image) {
            String v = value.toLowerCase();
            if (v.endsWith(".gif")) {
                return TableVisitHistory.update(ResourceType.File, FileType.Gif, OperationType.Access, value);
            } else if (v.endsWith(".tif") || v.endsWith(".tiff")) {
                return TableVisitHistory.update(ResourceType.File, FileType.Tif, OperationType.Access, value);
            } else {
                return TableVisitHistory.update(ResourceType.File, FileType.Image, OperationType.Access, value);
            }
        } else {
            return TableVisitHistory.update(ResourceType.File, fileType, OperationType.Access, value);
        }

    }

    public static boolean readFile(int fileType, String value) {

        if (fileType == FileType.MultipleFrames || fileType == FileType.Image) {
            String v = value.toLowerCase();
            if (v.endsWith(".gif")) {
                return TableVisitHistory.update(ResourceType.File, FileType.Gif, OperationType.Read, value);
            } else if (v.endsWith(".tif") || v.endsWith(".tiff")) {
                return TableVisitHistory.update(ResourceType.File, FileType.Tif, OperationType.Read, value);
            } else {
                return TableVisitHistory.update(ResourceType.File, FileType.Image, OperationType.Read, value);
            }
        } else {
            return TableVisitHistory.update(ResourceType.File, fileType, OperationType.Read, value);
        }

    }

    public static boolean writeFile(int fileType, String value) {

        if (fileType == FileType.MultipleFrames || fileType == FileType.Image) {
            String v = value.toLowerCase();
            if (v.endsWith(".gif")) {
                return TableVisitHistory.update(ResourceType.File, FileType.Gif, OperationType.Write, value);
            } else if (v.endsWith(".tif") || v.endsWith(".tiff")) {
                return TableVisitHistory.update(ResourceType.File, FileType.Tif, OperationType.Write, value);
            } else {
                return TableVisitHistory.update(ResourceType.File, FileType.Image, OperationType.Write, value);
            }
        } else {
            return TableVisitHistory.update(ResourceType.File, fileType, OperationType.Write, value);
        }
    }

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
