package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import javafx.stage.Window;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.data2d.DataFileText;
import mara.mybox.data2d.DataTable;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.fxml.style.StyleTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.UserConfig;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public class DataFileTextController extends BaseData2DFileController {

    protected DataFileText dataFileText;

    public DataFileTextController() {
        baseTitle = message("EditTextDataFile");
        TipsLabelKey = "DataFileTextTips";
    }

    @Override
    public void initData() {
        try {
            setDataType(Data2D.Type.Texts);
            dataFileText = (DataFileText) dataController.data2D;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    @Override
    public void pickRefreshOptions() {
        Charset charset;
        if (UserConfig.getBoolean(baseName + "SourceAutoDetermine", true)) {
            charset = TextFileTools.charset(dataFileText.getFile());
        } else {
            charset = Charset.forName(UserConfig.getString(baseName + "SourceCharset", "utf-8"));
        }
        dataFileText.setOptions(
                UserConfig.getBoolean(baseName + "SourceWithNames", true),
                charset,
                UserConfig.getString(baseName + "SourceDelimiter", ","));
    }

    @Override
    public Data2D saveAsTarget() {
        File file = chooseSaveFile();
        if (file == null) {
            return null;
        }
        DataFileText targetData = new DataFileText();
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
        dataFileText.initFile(file);
        dataFileText.setOptions(withName, charset, delimiter + "");
        dataController.readDefinition();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (!dataFileText.hasData() || !dataController.tableController.verifyData()) {
            return;
        }
        DataFileTextSaveAsController.open(this);
    }

    @Override
    public List<MenuItem> fileMenuItems(Event fevent) {
        List<MenuItem> items = new ArrayList<>();
        MenuItem menu;

        if (sourceFile != null) {
            menu = new MenuItem(message("Format"), StyleTools.getIconImageView("iconDelimiter.png"));
            menu.setOnAction((ActionEvent menuItemEvent) -> {
                DataFileTextFormatController.open(this);
            });
            items.add(menu);
        }

        items.addAll(super.fileMenuItems(fevent));
        return items;
    }

    /*
        static
     */
    public static DataFileTextController open(String name, List<Data2DColumn> cols, List<List<String>> data) {
        DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
        controller.dataController.loadTmpData(name, cols, data);
        controller.requestMouse();
        return controller;
    }

    public static DataFileTextController open(File file, Charset charset, boolean withNames, String delimiter) {
        DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
        controller.setFile(file, charset, withNames, delimiter);
        controller.requestMouse();
        return controller;
    }

    public static DataFileTextController open() {
        DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
        controller.createAction();
        controller.requestMouse();
        return controller;
    }

    public static DataFileTextController load(Window parent) {
        DataFileTextController controller = (DataFileTextController) WindowTools.replaceStage(parent, Fxmls.DataFileTextFxml);
        controller.createAction();
        controller.requestMouse();
        return controller;
    }

    public static DataFileTextController open(Data2DDefinition def) {
        DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
        controller.loadDef(def);
        controller.requestMouse();
        return controller;
    }

    public static DataFileTextController loadCSV(DataFileCSV csvData) {
        DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
        controller.loadCSVData(csvData);
        controller.requestMouse();
        return controller;
    }

    public static DataFileTextController loadTable(DataTable dataTable) {
        DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
        controller.loadTableData(dataTable);
        controller.requestMouse();
        return controller;
    }

}
