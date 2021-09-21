/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.db.data;

import java.io.File;
import java.sql.Connection;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.control.MenuItem;
import javafx.stage.FileChooser;
import mara.mybox.controller.BaseController;
import mara.mybox.db.DerbyBase;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.db.data.VisitHistory.OperationType;
import mara.mybox.db.data.VisitHistory.ResourceType;
import mara.mybox.db.table.TableVisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

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
        return UserConfig.getPath(getPathKey(type));
    }

    public static List<FileChooser.ExtensionFilter> getExtensionFilter(int fileType) {
        if (fileType == FileType.Image) {
            return FileFilters.ImageExtensionFilter;
        } else if (fileType == FileType.PDF) {
            return FileFilters.PdfExtensionFilter;
        } else if (fileType == FileType.Text) {
            return FileFilters.TextExtensionFilter;
        } else if (fileType == FileType.Bytes) {
            return FileFilters.AllExtensionFilter;
        } else if (fileType == FileType.Markdown) {
            return FileFilters.MarkdownExtensionFilter;
        } else if (fileType == FileType.Html) {
            return FileFilters.HtmlExtensionFilter;
        } else if (fileType == FileType.Gif) {
            return FileFilters.GifExtensionFilter;
        } else if (fileType == FileType.Tif) {
            return FileFilters.TiffExtensionFilter;
        } else if (fileType == FileType.MultipleFrames) {
            return FileFilters.MultipleFramesImageExtensionFilter;
        } else if (fileType == FileType.Media) {
            return FileFilters.JdkMediaExtensionFilter;
        } else if (fileType == FileType.Audio) {
            return FileFilters.SoundExtensionFilter;
        } else if (fileType == FileType.Icc) {
            return FileFilters.IccProfileExtensionFilter;
        } else if (fileType == FileType.Certificate) {
            return FileFilters.KeyStoreExtensionFilter;
        } else if (fileType == VisitHistory.FileType.TTC) {
            return FileFilters.TTCExtensionFilter;
        } else if (fileType == VisitHistory.FileType.TTF) {
            return FileFilters.TTFExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Excel) {
            return FileFilters.ExcelExtensionFilter;
        } else if (fileType == VisitHistory.FileType.CSV) {
            return FileFilters.CsvExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Sheet) {
            return FileFilters.SheetExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Cert) {
            return FileFilters.CertExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Word) {
            return FileFilters.WordExtensionFilter;
        } else if (fileType == VisitHistory.FileType.WordX) {
            return FileFilters.WordXExtensionFilter;
        } else if (fileType == VisitHistory.FileType.WordS) {
            return FileFilters.WordSExtensionFilter;
        } else if (fileType == VisitHistory.FileType.PPT) {
            return FileFilters.PPTExtensionFilter;
        } else if (fileType == VisitHistory.FileType.PPTX) {
            return FileFilters.PPTXExtensionFilter;
        } else if (fileType == VisitHistory.FileType.PPTS) {
            return FileFilters.PPTSExtensionFilter;
        } else if (fileType == VisitHistory.FileType.ImagesList) {
            return FileFilters.ImagesListExtensionFilter;
        } else {
            return FileFilters.AllExtensionFilter;
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

    public static List<MenuItem> getRecentMenu(BaseController controller) {
        List<MenuItem> menus = new ArrayList();
        List<VisitHistory> his = VisitHistoryTools.getRecentMenu();
        if (his == null || his.isEmpty()) {
            return menus;
        }
        List<String> valid = new ArrayList();
        for (VisitHistory h : his) {
            final String fname = h.getResourceValue();
            final String fxml = h.getDataMore();
            if (valid.contains(fxml)) {
                continue;
            }
            valid.add(fxml);
            MenuItem menu = new MenuItem(Languages.message(fname));
            menu.setOnAction(new EventHandler<ActionEvent>() {
                @Override
                public void handle(ActionEvent event) {
                    controller.loadScene(fxml);
                }
            });
            menus.add(menu);
        }
        return menus;

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
        try ( Connection conn = DerbyBase.getConnection()) {
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
        try ( Connection conn = DerbyBase.getConnection()) {
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
        try ( Connection conn = DerbyBase.getConnection()) {
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
        try ( Connection conn = DerbyBase.getConnection()) {
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
