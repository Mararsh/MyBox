package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.ArrayList;
import java.util.List;
import javafx.event.ActionEvent;
import javafx.event.Event;
import javafx.fxml.FXML;
import javafx.scene.control.MenuItem;
import mara.mybox.data2d.Data2D;
import mara.mybox.data2d.DataFileCSV;
import mara.mybox.db.data.Data2DColumn;
import mara.mybox.db.data.Data2DDefinition;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.style.StyleTools;
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

    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            dataFileCSV = (DataFileCSV) data2D;
        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.CSV);
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
//        dataFileCSV.setOptions(withName, charset, delimiter + "");
        readDefinition();
    }

    @FXML
    @Override
    public void saveAsAction() {
        if (!dataFileCSV.hasData() || !verifyData()) {
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
    public static DataFileCSVController open2() {
        return null;
    }

    public static DataFileCSVController open2(File file, Charset charset, boolean withNames, String delimiter) {
        DataFileCSVController controller = open2();
        if (controller != null) {
            controller.setFile(file, charset, withNames, delimiter);
        }
        return controller;
    }

    public static DataFileCSVController open2(String name, List<Data2DColumn> cols, List<List<String>> data) {
        DataFileCSVController controller = open2();
        if (controller != null) {
            controller.loadData(name, cols, data);
        }
        return controller;
    }

    public static DataFileCSVController openFile2(File file) {
        DataFileCSVController controller = open2();
        if (controller != null) {
            controller.sourceFileChanged(file);
        }
        return controller;
    }

    public static DataFileCSVController open2(Data2DDefinition def) {
        DataFileCSVController controller = open2();
        if (controller != null) {
            controller.loadDef(def);
        }
        return controller;
    }

}
