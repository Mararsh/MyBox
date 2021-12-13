package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import java.util.List;
import javafx.fxml.FXML;
import javafx.stage.Window;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataFileText;
import mara.mybox.db.data.Data2DColumn;
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
    protected ControlTextOptions readOptionsController;

    public DataFileTextController() {
        baseTitle = message("EditTextDataFile");
    }

    @Override
    public void initValues() {
        try {
            super.initValues();

            setDataType(Data2D.Type.Text);
            dataFileText = (DataFileText) dataController.data2D;
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
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

            readOptionsController.setControls(baseName + "Read", true);

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public void pickOptions() {
        Charset charset;
        if (readOptionsController.autoDetermine) {
            charset = TextFileTools.charset(dataFileText.getFile());
        } else {
            charset = readOptionsController.charset;
        }
        dataFileText.setOptions(readOptionsController.withNamesCheck.isSelected(),
                charset, readOptionsController.delimiterName);
    }

    public void setFile(File file, Charset charset, boolean withName, String delimiter) {
        if (file == null || !checkBeforeNextAction()) {
            return;
        }
        readOptionsController.withNamesCheck.setSelected(withName);
        readOptionsController.setDelimiter(delimiter);
        readOptionsController.setCharset(charset);
        dataFileText.initFile(file);
        dataFileText.setOptions(withName, charset, delimiter + "");
        dataController.readDefinition();
    }


    /*
        static
     */
    public static DataFileTextController open(List<Data2DColumn> cols, List<List<String>> data) {
        DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
        controller.dataController.loadTmpData(cols, data);
        return controller;
    }

    public static DataFileTextController open(File file, Charset charset, boolean withNames, String delimiter) {
        DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
        controller.setFile(file, charset, withNames, delimiter);
        return controller;
    }

    public static DataFileTextController open() {
        DataFileTextController controller = (DataFileTextController) WindowTools.openStage(Fxmls.DataFileTextFxml);
        controller.createAction();
        return controller;
    }

    public static DataFileTextController load(Window parent) {
        DataFileTextController controller = (DataFileTextController) WindowTools.openScene(parent, Fxmls.DataFileTextFxml);
        controller.createAction();
        return controller;
    }

}
