package mara.mybox.controller;

import java.io.File;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileExcel;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-1-17
 * @License Apache License Version 2.0
 */
public class DataFileExcelController extends BaseData2DFileController {

    protected DataFileExcel dataFileExcel;

    public DataFileExcelController() {
        baseTitle = message("EditExcel");
        TipsLabelKey = "DataFileExcelTips";
    }

    @Override
    public void initData() {
        try {
            setDataType(Data2D.Type.Excel);
            dataFileExcel = (DataFileExcel) dataController.data2D;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Excel);
    }

    @Override
    public void pickRefreshOptions() {
        dataFileExcel.setOptions(UserConfig.getBoolean(baseName + "SourceWithNames", true));
    }

    @Override
    public Data2D saveAsTarget() {
        File file = chooseSaveFile();
        if (file == null) {
            return null;
        }
        DataFileExcel targetData = new DataFileExcel();
        targetData.setCurrentSheetOnly(UserConfig.getBoolean(baseName + "TargetWithNames", true))
                .initFile(file).setSheet(dataFileExcel.getSheet())
                .setHasHeader(UserConfig.getBoolean(baseName + "CurrentOnly", false));
        return targetData;
    }

    public void loadSheetName(String name) {
        try {
            if (!checkBeforeNextAction() || name == null) {
                return;
            }
            dataFileExcel.initFile(dataFileExcel.getFile(), name);
            dataController.readDefinition();
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    public void setFile(File file, boolean withName) {
        if (file == null || !checkBeforeNextAction()) {
            return;
        }
        dataFileExcel.initFile(file);
        dataFileExcel.setOptions(withName);
        dataController.readDefinition();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (!dataFileExcel.hasData() || !dataController.tableController.verifyData()) {
            return;
        }
        DataFileExcelSaveAsController.open(this);
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        if (sourceFile != null) {
            menu = new MenuItem(message("Sheet"), StyleTools.getIconImageView("iconFrame.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                DataFileExcelSheetsController.open(this);
            });
            items.add(menu);

            menu = new MenuItem(message("Format"), StyleTools.getIconImageView("iconDelimiter.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                DataFileExcelFormatController.open(this);
            });
            items.add(menu);
        }

        items.addAll(super.fileMenuItems(fevent));
        return items;
    }

    /*
        static
     */
    public static DataFileExcelController open() {
        try {
            DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
            if (controller != null) {
                controller.requestMouse();
            }
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

    public static DataFileExcelController openFile(File file) {
        DataFileExcelController controller = open();
        if (controller != null) {
            controller.sourceFileChanged(file);
        }
        return controller;
    }

    public static DataFileExcelController open(File file, boolean withNames) {
        DataFileExcelController controller = open();
        if (controller != null) {
            controller.setFile(file, withNames);
        }
        return controller;
    }

    public static DataFileExcelController open(String name, List<Data2DColumn> cols, List<List<String>> data) {
        DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        if (controller != null) {
            controller.dataController.loadTmpData(name, cols, data);
        }
        return controller;
    }

    public static DataFileExcelController open(Data2DDefinition def) {
        DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        if (controller != null) {
            controller.loadDef(def);
        }
        return controller;
    }

    public static DataFileExcelController loadCSV(DataFileCSV csvData) {
        DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        if (controller != null) {
            controller.loadCSVData(csvData);
        }
        return controller;
    }

    public static DataFileExcelController loadTable(DataTable dataTable) {
        DataFileExcelController controller = (DataFileExcelController) WindowTools.openStage(Fxmls.DataFileExcelFxml);
        if (controller != null) {
            controller.loadTableData(dataTable);
        }
        return controller;
    }

}
