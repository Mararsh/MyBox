/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.tools;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.VisitHistory.FileType;
import mara.mybox.data.VisitHistory.OperationType;
import mara.mybox.data.VisitHistory.ResourceType;
import mara.mybox.db.TableVisitHistory;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonFxValues;

/**
 *
 * @author mara
 */
public class VisitHistoryTools {

    public static String getPathKey(int type) {
        return "FilePath" + type;
    }

    public static File getSavedPath(int type) {
        return AppVariables.getUserConfigPath(getPathKey(type));
    }

    public static String getSaveAsOptionsKey(int type) {
        return "SaveAs" + type;
    }

    public static List<FileChooser.ExtensionFilter> getExtensionFilter(int fileType) {
        if (fileType == FileType.Image) {
            return CommonFxValues.ImageExtensionFilter;
        } else if (fileType == FileType.PDF) {
            return CommonFxValues.ImageExtensionFilter;
        } else if (fileType == FileType.Text) {
            return CommonFxValues.TextExtensionFilter;
        } else if (fileType == FileType.Bytes) {
            return CommonFxValues.AllExtensionFilter;
        } else if (fileType == FileType.Markdown) {
            return CommonFxValues.MarkdownExtensionFilter;
        } else if (fileType == FileType.Html) {
            return CommonFxValues.HtmlExtensionFilter;
        } else if (fileType == FileType.Gif) {
            return CommonFxValues.GifExtensionFilter;
        } else if (fileType == FileType.Tif) {
            return CommonFxValues.TiffExtensionFilter;
        } else if (fileType == FileType.Media) {
            return CommonFxValues.JdkMediaExtensionFilter;
        } else if (fileType == FileType.Icc) {
            return CommonFxValues.IccProfileExtensionFilter;
        } else if (fileType == FileType.Certificate) {
            return CommonFxValues.KeyStoreExtensionFilter;
        } else if (fileType == FileType.TextEditHistory) {
            return CommonFxValues.TextExtensionFilter;
        } else {
            return CommonFxValues.AllExtensionFilter;
        }
    }

    public static List<VisitHistory> getRecentPathRead(int fileType) {
        if (AppVariables.fileRecentNumber <= 0) {
            return null;
        }
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Read, AppVariables.fileRecentNumber);
    }

    public static List<VisitHistory> getFile(int fileType) {
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.File, types, 0);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.File, types, 0);
        } else if (fileType == FileType.Media) {
            int[] types = {FileType.Media, FileType.Video, FileType.Audio};
            return TableVisitHistory.find(ResourceType.File, types, 0);
        } else {
            return TableVisitHistory.find(ResourceType.File, fileType, 0);
        }
    }

    public static List<VisitHistory> getRecentMenu() {
        return TableVisitHistory.find(ResourceType.Menu, 15);
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

    public static List<VisitHistory> getRecentFileRead(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Read, AppVariables.fileRecentNumber);
    }

    public static List<VisitHistory> getLastFileRead(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Read, 1);
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

    public static List<VisitHistory> getPathRead(int fileType) {
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Read, 0);
    }

    public static List<VisitHistory> getFileWritten(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Write, 0);
    }

    public static boolean readPath(int fileType, String value) {
        if (fileType == FileType.MultipleFrames) {
            return TableVisitHistory.update(ResourceType.Path, FileType.Image, OperationType.Read, value);
        } else {
            return TableVisitHistory.update(ResourceType.Path, fileType, OperationType.Read, value);
        }
    }

    public static List<VisitHistory> getPath(int fileType) {
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.Path, types, 0);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.Path, types, 0);
        } else if (fileType == FileType.Media) {
            int[] types = {FileType.Media, FileType.Video, FileType.Audio};
            return TableVisitHistory.find(ResourceType.Path, types, 0);
        } else {
            return TableVisitHistory.find(ResourceType.Path, fileType, 0);
        }
    }

    public static List<VisitHistory> getLastPathRead(int fileType) {
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Read, 1);
    }

    public static List<VisitHistory> getLastPathWritten(int fileType) {
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Write, 1);
    }

    public static List<VisitHistory> getLastFile(int fileType) {
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.File, types, 1);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.File, types, 1);
        } else if (fileType == FileType.Media) {
            int[] types = {FileType.Media, FileType.Video, FileType.Audio};
            return TableVisitHistory.find(ResourceType.File, types, 1);
        } else {
            return TableVisitHistory.find(ResourceType.File, fileType, 1);
        }
    }

    public static List<VisitHistory> getRecentAlphaImages(int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records = TableVisitHistory.findAlphaImages(number);
        return checkFilesExisted(records);
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

    public static List<VisitHistory> getPathWritten(int fileType) {
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Write, 0);
    }

    public static List<VisitHistory> getRecentFile(int fileType) {
        return getRecentFile(fileType, AppVariables.fileRecentNumber);
    }

    public static List<VisitHistory> getRecentFile(int fileType, int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records;
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            records = TableVisitHistory.find(ResourceType.File, types, number);
        } else if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            records = TableVisitHistory.find(ResourceType.File, types, number);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            records = TableVisitHistory.find(ResourceType.File, types, number);
        } else if (fileType == FileType.Media) {
            int[] types = {FileType.Media, FileType.Video, FileType.Audio};
            records = TableVisitHistory.find(ResourceType.File, types, number);
        } else {
            records = TableVisitHistory.find(ResourceType.File, fileType, number);
        }
        return checkFilesExisted(records);
    }

    public static List<VisitHistory> getRecentStreamMedia() {
        return TableVisitHistory.find(ResourceType.URI, FileType.StreamMedia, AppVariables.fileRecentNumber);
    }

    public static List<VisitHistory> checkFilesExisted(List<VisitHistory> records) {
        if (records == null || records.isEmpty()) {
            return records;
        }
        List<VisitHistory> valid = new ArrayList<>();
        List<String> names = new ArrayList<>();
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

    public static List<VisitHistory> getLastPath(int fileType) {
        if (fileType == FileType.MultipleFrames) {
            int[] types = {FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.Path, types, 1);
        } else if (fileType == FileType.Image) {
            int[] types = {FileType.Image, FileType.Gif, FileType.Tif, FileType.MultipleFrames};
            return TableVisitHistory.find(ResourceType.Path, types, 1);
        } else if (fileType == FileType.Media) {
            int[] types = {FileType.Media, FileType.Video, FileType.Audio};
            return TableVisitHistory.find(ResourceType.Path, types, 1);
        } else {
            return TableVisitHistory.find(ResourceType.Path, fileType, 1);
        }
    }

    public static List<VisitHistory> getRecentFileWritten(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Write, AppVariables.fileRecentNumber);
    }

    public static boolean visitPath(int fileType, String value) {
        if (fileType == FileType.MultipleFrames) {
            return TableVisitHistory.update(ResourceType.Path, FileType.Image, OperationType.Access, value);
        } else {
            return TableVisitHistory.update(ResourceType.Path, fileType, OperationType.Access, value);
        }
    }

    public static List<VisitHistory> getRecentPathWritten(int fileType) {
        if (AppVariables.fileRecentNumber <= 0) {
            return null;
        }
        return TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Write, AppVariables.fileRecentNumber);
    }

    public static List<VisitHistory> getFileRead(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Read, 0);
    }

    public static boolean visitMenu(String name, String fxml) {
        return TableVisitHistory.update(ResourceType.Menu, FileType.General, OperationType.Access, name, fxml);
    }

    public static List<VisitHistory> getLastFileWritten(int fileType) {
        return TableVisitHistory.find(ResourceType.File, fileType, OperationType.Write, 1);
    }

    public static boolean visitStreamMedia(String address) {
        return TableVisitHistory.update(ResourceType.URI, FileType.StreamMedia, OperationType.Access, address);
    }

    public static List<VisitHistory> getRecentPath(int fileType) {
        return getRecentPath(fileType, AppVariables.fileRecentNumber);
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
        } else if (fileType == FileType.Media) {
            int[] types = {FileType.Media, FileType.Video, FileType.Audio};
            records = TableVisitHistory.find(ResourceType.Path, types, number);
        } else {
            records = TableVisitHistory.find(ResourceType.Path, fileType, number);
        }
        return checkFilesExisted(records);
    }

}
