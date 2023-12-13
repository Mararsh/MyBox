package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import javafx.fxml.FXML;
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
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2021-8-25
 * @License Apache License Version 2.0
 */
public class DataFileTextController extends BaseData2DFileController {

    protected DataFileText dataFileText;

    @FXML
    protected ControlTextOptions readOptionsController, writeOptionsController;

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
    public void initControls() {
        try {
            super.initControls();

            readOptionsController.setControls(baseName + "Read", true, true);
            writeOptionsController.setControls(baseName + "Write", false, true);

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @Override
    public void pickRefreshOptions() {
        Charset charset;
        if (readOptionsController.autoDetermine) {
            charset = TextFileTools.charset(dataFileText.getFile());
        } else {
            charset = readOptionsController.charset;
        }
        dataFileText.setOptions(readOptionsController.withNamesCheck.isSelected(),
                charset, readOptionsController.getDelimiterName());
    }

    @Override
    public Data2D saveAsTarget() {
        File file = chooseSaveFile();
        if (file == null) {
            return null;
        }
        DataFileText targetData = new DataFileText();
        targetData.initFile(file)
                .setCharset(writeOptionsController.charset)
                .setDelimiter(writeOptionsController.getDelimiterName())
                .setHasHeader(writeOptionsController.withNamesCheck.isSelected());
        return targetData;
    }

    public void setFile(File file, Charset charset, boolean withName, String delimiter) {
        if (file == null || !checkBeforeNextAction()) {
            return;
        }
        readOptionsController.withNamesCheck.setSelected(withName);
        readOptionsController.setDelimiterName(delimiter);
        readOptionsController.setCharset(charset);
        dataFileText.initFile(file);
        dataFileText.setOptions(withName, charset, delimiter + "");
        dataController.readDefinition();
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
