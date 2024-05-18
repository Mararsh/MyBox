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
import static mara.mybox.db.data.VisitHistory.Default_Max_Histories;
import mara.mybox.db.data.VisitHistory.FileType;
import mara.mybox.db.data.VisitHistory.OperationType;
import mara.mybox.db.data.VisitHistory.ResourceType;
import mara.mybox.db.table.TableVisitHistory;
import mara.mybox.tools.FileNameTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.FileFilters;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;

/**
 *
 * @author mara
 */
public class VisitHistoryTools {

    public static final TableVisitHistory VisitHistories = new TableVisitHistory();

    /*
        base values
     */
    public static String getPathKey(int type) {
        return "FilePath" + type;
    }

    public static String defaultPath(int type) {
        return FileTmpTools.generatePath(defaultExt(type));
    }

    public static File getSavedPath(int type) {
        return UserConfig.getPath(getPathKey(type), defaultPath(type));
    }

    public static String defaultExt(int type) {
        List<FileChooser.ExtensionFilter> filters = getExtensionFilter(type);
        if (filters == null || filters.isEmpty()) {
            return null;
        }
        String ext = filters.get(0).getExtensions().get(0);
        if (ext.endsWith("*")) {
            if (filters.size() > 1) {
                ext = filters.get(1).getExtensions().get(0);
            } else {
                return null;
            }
        }
        return FileNameTools.ext(ext);
    }

    public static List<FileChooser.ExtensionFilter> getExtensionFilter(int fileType) {
        if (fileType == FileType.Image) {
            return FileFilters.ImageExtensionFilter;
        } else if (fileType == FileType.PDF) {
            return FileFilters.PdfExtensionFilter;
        } else if (fileType == FileType.Text) {
            return FileFilters.TextExtensionFilter;
        } else if (fileType == FileType.All) {
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
        } else if (fileType == VisitHistory.FileType.Jar) {
            return FileFilters.JarExtensionFilter;
        } else if (fileType == VisitHistory.FileType.DataFile) {
            return FileFilters.DataFileExtensionFilter;
        } else if (fileType == VisitHistory.FileType.JSON) {
            return FileFilters.JSONExtensionFilter;
        } else if (fileType == VisitHistory.FileType.XML) {
            return FileFilters.XMLExtensionFilter;
        } else if (fileType == VisitHistory.FileType.SVG) {
            return FileFilters.SVGExtensionFilter;
        } else if (fileType == VisitHistory.FileType.Javascript) {
            return FileFilters.JavascriptExtensionFilter;
        } else {
            return FileFilters.AllExtensionFilter;
        }
    }

    public static boolean validFile(String name, int resourceType, int fileType) {
        if (resourceType != ResourceType.File && resourceType != ResourceType.Path) {
            return true;
        }
        if (name == null) {
            return false;
        }
        File file = new File(name);
        if (!file.exists()) {
            return false;
        }
        if (resourceType == ResourceType.Path) {
            return true;
        }
        String suffix = FileNameTools.ext(file.getName());
        if (fileType == FileType.PDF) {
            if (!suffix.equalsIgnoreCase("pdf")) {
                return false;
            }
        } else if (fileType == FileType.Tif) {
            if (!suffix.equalsIgnoreCase("tif") && !suffix.equalsIgnoreCase("tiff")) {
                return false;
            }
        } else if (fileType == FileType.Gif) {
            if (!suffix.equalsIgnoreCase("gif")) {
                return false;
            }
        } else if (fileType == FileType.Html) {
            if (!suffix.equalsIgnoreCase("html") && !suffix.equalsIgnoreCase("htm")) {
                return false;
            }
        } else if (fileType == FileType.XML) {
            if (!suffix.equalsIgnoreCase("xml")) {
                return false;
            }
        } else if (fileType == FileType.Markdown) {
            if (!suffix.equalsIgnoreCase("md")) {
                return false;
            }
        } else if (fileType == FileType.TTC) {
            if (!suffix.equalsIgnoreCase("ttc")) {
                return false;
            }
        } else if (fileType == FileType.TTF) {
            if (!suffix.equalsIgnoreCase("ttf")) {
                return false;
            }
        } else if (fileType == FileType.Excel) {
            if (!suffix.equalsIgnoreCase("xls") && !suffix.equalsIgnoreCase("xlsx")) {
                return false;
            }
        } else if (fileType == FileType.CSV) {
            if (!suffix.equalsIgnoreCase("csv")) {
                return false;
            }
        } else if (fileType == FileType.Word) {
            if (!suffix.equalsIgnoreCase("doc")) {
                return false;
            }
        } else if (fileType == FileType.WordX) {
            if (!suffix.equalsIgnoreCase("docx")) {
                return false;
            }
        } else if (fileType == FileType.WordS) {
            if (!suffix.equalsIgnoreCase("doc") && !suffix.equalsIgnoreCase("docx")) {
                return false;
            }
        } else if (fileType == FileType.PPT) {
            if (!suffix.equalsIgnoreCase("ppt")) {
                return false;
            }
        } else if (fileType == FileType.PPTX) {
            if (!suffix.equalsIgnoreCase("pptx")) {
                return false;
            }
        } else if (fileType == FileType.PPTS) {
            if (!suffix.equalsIgnoreCase("ppt") && !suffix.equalsIgnoreCase("pptx")) {
                return false;
            }
        } else if (fileType == FileType.SVG) {
            if (!suffix.equalsIgnoreCase("svg")) {
                return false;
            }
        }
        return true;
    }

    /*
        Menu
     */
    public static boolean visitMenu(String name, String fxml) {
        return VisitHistories.update(ResourceType.Menu, FileType.General, OperationType.Access, name, fxml);
    }

    public static List<VisitHistory> getRecentMenu() {
        return VisitHistories.read(ResourceType.Menu, Default_Max_Histories);
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
        Files
     */
    public static boolean readFile(Connection conn, int fileType, String value) {
        return VisitHistories.update(conn, ResourceType.File, fileType, OperationType.Read, value);
    }

    public static boolean writeFile(Connection conn, int fileType, String value) {
        return VisitHistories.update(conn, ResourceType.File, fileType, OperationType.Write, value);
    }

    public static boolean visitStreamMedia(String address) {
        return VisitHistories.update(ResourceType.URI, FileType.StreamMedia, OperationType.Access, address);
    }

    public static List<VisitHistory> getRecentFileRead(int fileType, int count) {
        if (count <= 0) {
            return null;
        }
        List<VisitHistory> records = VisitHistories.read(ResourceType.File, fileType, OperationType.Read, count);
        return records;
    }

    public static List<VisitHistory> getRecentFileWrite(int fileType, int count) {
        if (count <= 0) {
            return null;
        }
        List<VisitHistory> records = VisitHistories.read(ResourceType.File, fileType, OperationType.Write, count);
        return records;
    }

    public static List<VisitHistory> getRecentStreamMedia() {
        if (AppVariables.fileRecentNumber <= 0) {
            return null;
        }
        List<VisitHistory> records = VisitHistories.read(ResourceType.URI, FileType.StreamMedia,
                AppVariables.fileRecentNumber);
        return records;
    }

    public static List<VisitHistory> getRecentAlphaImages(int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records = VisitHistories.readAlphaImages(number);
        return records;
    }

    /*
        Paths
     */
    public static boolean readPath(Connection conn, int fileType, String value) {
        return VisitHistories.update(conn, ResourceType.Path, fileType, OperationType.Read, value);
    }

    public static boolean writePath(Connection conn, int fileType, String value) {
        return VisitHistories.update(conn, ResourceType.Path, fileType, OperationType.Write, value);
    }

    public static List<VisitHistory> getRecentPathRead(int fileType) {
        return getRecentPathRead(fileType, AppVariables.fileRecentNumber);
    }

    public static List<VisitHistory> getRecentPathRead(int fileType, int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records = VisitHistories.read(ResourceType.Path, fileType, OperationType.Read, number);
        return records;
    }

    public static List<VisitHistory> getRecentPathWrite(int fileType) {
        return getRecentPathWrite(fileType, AppVariables.fileRecentNumber);
    }

    public static List<VisitHistory> getRecentPathWrite(int fileType, int number) {
        if (number <= 0) {
            return null;
        }
        List<VisitHistory> records = VisitHistories.read(ResourceType.Path, fileType, OperationType.Write, number);
        return records;
    }

}
