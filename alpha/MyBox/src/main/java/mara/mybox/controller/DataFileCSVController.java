package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.text.MessageFormat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashMap;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import mara.mybox.data.StringTable;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.FileTmpTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2020-12-24
 * @License Apache License Version 2.0
 */
public class DataFileCSVController extends BaseData2DFileController {

    protected DataFileCSV dataFileCSV;

    public DataFileCSVController() {
        baseTitle = message("EditCSV");
        TipsLabelKey = "DataFileCSVTips";
        dataType = Data2DDefinition.Type.CSV;
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataFileCSV = (DataFileCSV) dataController.data2D;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
    }

    @Override
    public void pickRefreshOptions() {
        Charset charset;
        if (UserConfig.getBoolean(baseName + "SourceAutoDetermine", true)) {
            charset = TextFileTools.charset(dataFileCSV.getFile());
        } else {
            charset = Charset.forName(UserConfig.getString(baseName + "SourceCharset", "utf-8"));
        }
        dataFileCSV.setOptions(
                UserConfig.getBoolean(baseName + "SourceWithNames", true),
                charset,
                UserConfig.getString(baseName + "SourceDelimiter", ","));
    }

    @Override
    public Data2D saveAsTarget() {
        File file = chooseSaveFile(dataFileCSV.dataName());
        if (file == null) {
            return null;
        }
        DataFileCSV targetData = new DataFileCSV();
        targetData.initFile(file)
                .setCharset(Charset.forName(UserConfig.getString(baseName + "TargetCharset", "utf-8")))
                .setDelimiter(UserConfig.getString(baseName + "TargetDelimiter", ","))
                .setHasHeader(UserConfig.getBoolean(baseName + "TargetWithNames", true));
        return targetData;
    }

    public void setFile(File file, Charset charset, boolean withName, String delimiter) {
        if (file == null || !checkBeforeNextAction()) {
            return;
        }
        dataFileCSV.initFile(file);
        dataFileCSV.setOptions(withName, charset, delimiter + "");
        dataController.readDefinition();
    }

    public void loadData(List<StringTable> tables) {
        if (tables == null || tables.isEmpty()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {

            private File filePath;
            private LinkedHashMap<File, Boolean> files;
            private int count;

            @Override
            protected boolean handle() {
                filePath = new File(FileTmpTools.generatePath("csv"));
                files = DataFileCSV.save(this, filePath, "tmp", tables);
                count = files != null ? files.size() : 0;
                return count > 0;
            }

            @Override
            protected void whenSucceeded() {
                Iterator<File> iterator = files.keySet().iterator();
                File csvFile = iterator.next();
                setFile(csvFile, Charset.forName("UTF-8"), files.get(csvFile), ",");
                if (count > 1) {
                    browseURI(filePath.toURI());
                    String info = MessageFormat.format(message("GeneratedFilesResult"),
                            count, "\"" + filePath + "\"");
                    int num = 1;
                    info += "\n    " + csvFile.getName();
                    while (iterator.hasNext()) {
                        info += "\n    " + iterator.next().getName();
                        if (++num > 10) {
                            info += "\n    ......";
                            break;
                        }
                    }
                    alertInformation(info);
                }
            }

        };
        start(task);
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (!dataFileCSV.hasData() || !dataController.verifyData()) {
            return;
        }
        DataFileCSVSaveAsController.open(this);
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        if (sourceFile != null) {
            menu = new MenuItem(message("Format"), StyleTools.getIconImageView("iconFormat.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                DataFileCSVFormatController.open(this);
            });
            items.add(menu);
        }

        items.addAll(super.fileMenuItems(fevent));
        return items;
    }

    /*
        static
     */
    public static DataFileCSVController open() {
        try {
            DataFileCSVController controller = (DataFileCSVController) WindowTools.openStage(Fxmls.DataFileCSVFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataFileCSVController open(File file, Charset charset, boolean withNames, String delimiter) {
        DataFileCSVController controller = open();
        if (controller != null) {
            controller.setFile(file, charset, withNames, delimiter);
        }
        return controller;
    }

    public static DataFileCSVController open(String name, List<Data2DColumn> cols, List<List<String>> data) {
        DataFileCSVController controller = open();
        if (controller != null) {
            controller.dataController.loadTmpData(name, cols, data);
        }
        return controller;
    }

    public static DataFileCSVController openFile(File file) {
        DataFileCSVController controller = open();
        if (controller != null) {
            controller.sourceFileChanged(file);
        }
        return controller;
    }

    public static DataFileCSVController open(Data2DDefinition def) {
        DataFileCSVController controller = open();
        if (controller != null) {
            controller.loadDef(def);
        }
        return controller;
    }

    public static DataFileCSVController loadCSV(DataFileCSV csvData) {
        DataFileCSVController controller = open();
        if (controller != null) {
            controller.loadCSVData(csvData);
        }
        return controller;
    }

    public static DataFileCSVController loadTable(DataTable dataTable) {
        DataFileCSVController controller = open();
        if (controller != null) {
            controller.loadTableData(dataTable);
        }
        return controller;
    }

}
