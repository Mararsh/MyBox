/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.data.tools;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.util.ArrayList;
import java.util.List;
import javafx.stage.FileChooser;
import mara.mybox.data.VisitHistory;
import mara.mybox.data.VisitHistory.FileType;
import mara.mybox.data.VisitHistory.OperationType;
import mara.mybox.data.VisitHistory.ResourceType;
import static mara.mybox.db.DerbyBase.dbHome;

import static mara.mybox.db.DerbyBase.login;
import static mara.mybox.db.DerbyBase.protocol;
import mara.mybox.db.TableVisitHistory;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.CommonFxValues;

/**
 *
 * @author mara
 */
public class VisitHistoryTools {

    /*
        base values
     */
    public static String getPathKey(int type) {
        return "FilePath" + type;
    }

    public static File getSavedPath(int type) {
        return AppVariables.getUserConfigPath(getPathKey(type));
    }

    public static List<FileChooser.ExtensionFilter> getExtensionFilter(int fileType) {
        if (fileType == FileType.Image) {
            return CommonFxValues.ImageExtensionFilter;
        } else if (fileType == FileType.PDF) {
            return CommonFxValues.PdfExtensionFilter;
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
        } else if (fileType == FileType.FileHistory) {
            return CommonFxValues.TextExtensionFilter;
        } else {
            return CommonFxValues.AllExtensionFilter;
        }
    }

    /*
        Menu
     */
    public static boolean visitMenu(String name, String fxml) {
        return TableVisitHistory.update(ResourceType.Menu, FileType.General, OperationType.Access, name, fxml);
    }

    public static List<VisitHistory> getRecentMenu() {
        return TableVisitHistory.find(ResourceType.Menu, 15);
    }

    /*
        URI
     */
    public static boolean visitURI(String address) {
        return TableVisitHistory.update(ResourceType.URI, FileType.General, OperationType.Download, address);
    }

    public static List<VisitHistory> recentDownload() {
        return TableVisitHistory.find(ResourceType.URI, FileType.General, OperationType.Download, 15);
    }

    public static List<String> recentDownloadAddress() {
        List<VisitHistory> records = recentDownload();
        List<String> addresses = new ArrayList<>();
        if (records != null) {
            for (VisitHistory r : records) {
                addresses.add(r.getResourceValue());
            }
        }
        return addresses;
    }

    /*
        Files
     */
    public static boolean readFile(int fileType, String value) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return readFile(conn, fileType, value);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean readFile(Connection conn, int fileType, String value) {
        return TableVisitHistory.update(conn, ResourceType.File, fileType, OperationType.Read, value);
    }

    public static boolean writeFile(int fileType, String value) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return writeFile(conn, fileType, value);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean writeFile(Connection conn, int fileType, String value) {
        return TableVisitHistory.update(conn, ResourceType.File, fileType, OperationType.Write, value);
    }

    public static boolean visitFile(int fileType, String value) {
        return TableVisitHistory.update(ResourceType.File, fileType, OperationType.Access, value);
    }

    public static boolean visitStreamMedia(String address) {
        return TableVisitHistory.update(ResourceType.URI, FileType.StreamMedia, OperationType.Access, address);
    }

    public static List<VisitHistory> getFile(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.File, fileType, 0);
        return records;
    }

    public static List<VisitHistory> getRecentFile(int fileType) {
        return getRecentFile(fileType, AppVariables.fileRecentNumber);
    }

    public static List<VisitHistory> getRecentFile(int fileType, int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.File, fileType, number);
        return records;
    }

    public static List<VisitHistory> getRecentReadWrite(int fileType, int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> read = getRecentFileRead(fileType, number / 2 + 1);
        List<VisitHistory> write = getRecentFileWrite(fileType, number / 4 + 1);
        List<VisitHistory> records;
        if (read != null) {
            records = read;
            if (write != null) {
                records.addAll(write);
            }
        } else {
            records = write;
        }
        return records;
    }

    public static List<VisitHistory> getRecentFileRead(int fileType, int count) {
        if (count <= 0) {
            return null;
        }
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.File, fileType, OperationType.Read, count);
        return records;
    }

    public static List<VisitHistory> getRecentFileWrite(int fileType, int count) {
        if (count <= 0) {
            return null;
        }
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.File, fileType, OperationType.Write, count);
        return records;
    }

    public static List<VisitHistory> getRecentFileWrite(int fileType) {
        return getRecentFileWrite(fileType, AppVariables.fileRecentNumber);
    }

    public static List<VisitHistory> getRecentStreamMedia() {
        if (AppVariables.fileRecentNumber <= 0) {
            return null;
        }
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.URI, FileType.StreamMedia, AppVariables.fileRecentNumber);
        return records;
    }

    public static List<VisitHistory> getRecentAlphaImages(int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records = TableVisitHistory.findAlphaImages(number);
        return records;
    }

    public static List<VisitHistory> getFileRead(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.File, fileType, OperationType.Read, 0);
        return records;
    }

    public static List<VisitHistory> getFileWritten(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.File, fileType, OperationType.Write, 0);
        return records;
    }

    public static VisitHistory getLastFile(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.File, fileType, 1);
        if (records != null && !records.isEmpty()) {
            return records.get(0);
        } else {
            return null;
        }
    }

    public static VisitHistory getLastFileRead(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.File, fileType, OperationType.Read, 1);
        if (records != null && !records.isEmpty()) {
            return records.get(0);
        } else {
            return null;
        }
    }

    public static VisitHistory getLastFileWritten(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.File, fileType, OperationType.Write, 1);
        if (records != null && !records.isEmpty()) {
            return records.get(0);
        } else {
            return null;
        }
    }


    /*
        Paths
     */
    public static boolean readPath(int fileType, String value) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return readPath(conn, fileType, value);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean readPath(Connection conn, int fileType, String value) {
        return TableVisitHistory.update(conn, ResourceType.Path, fileType, OperationType.Read, value);
    }

    public static boolean writePath(int fileType, String value) {
        try ( Connection conn = DriverManager.getConnection(protocol + dbHome() + login)) {
            return writePath(conn, fileType, value);
        } catch (Exception e) {
            MyBoxLog.error(e);
            return false;
        }
    }

    public static boolean writePath(Connection conn, int fileType, String value) {
        return TableVisitHistory.update(conn, ResourceType.Path, fileType, OperationType.Write, value);
    }

    public static boolean visitPath(int fileType, String value) {
        return TableVisitHistory.update(ResourceType.Path, fileType, OperationType.Access, value);
    }

    public static List<VisitHistory> getPath(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.Path, fileType, 0);
        return records;
    }

    public static List<VisitHistory> getRecentPathRead(int fileType) {
        if (AppVariables.fileRecentNumber <= 0) {
            return null;
        }
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Read, AppVariables.fileRecentNumber);
        return records;
    }

    public static List<VisitHistory> getRecentPathWritten(int fileType) {
        if (AppVariables.fileRecentNumber <= 0) {
            return null;
        }
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Write, AppVariables.fileRecentNumber);
        return records;
    }

    public static List<VisitHistory> getRecentPath(int fileType) {
        return getRecentPath(fileType, AppVariables.fileRecentNumber);
    }

    public static List<VisitHistory> getRecentPath(int fileType, int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.Path, fileType, number);
        return records;
    }

    public static List<VisitHistory> getPathRead(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Read, 0);
        return records;
    }

    public static List<VisitHistory> getPathWritten(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Write, 0);
        return records;
    }

    public static VisitHistory getLastPathRead(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Read, 1);
        if (records != null && !records.isEmpty()) {
            return records.get(0);
        } else {
            return null;
        }
    }

    public static VisitHistory getLastPathWritten(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.Path, fileType, OperationType.Write, 1);
        if (records != null && !records.isEmpty()) {
            return records.get(0);
        } else {
            return null;
        }
    }

    public static VisitHistory getLastPath(int fileType) {
        List<VisitHistory> records = TableVisitHistory.find(ResourceType.Path, fileType, 1);
        if (records != null && !records.isEmpty()) {
            return records.get(0);
        } else {
            return null;
        }
    }

}
