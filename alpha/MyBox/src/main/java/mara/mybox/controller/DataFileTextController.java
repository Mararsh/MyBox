package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import mara.mybox.data.Data2D;
import mara.mybox.data.DataFileText;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.TextFileTools;
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
            writeOptionsController.setControls(baseName + "Write", false);
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

    @Override
    public Data2D makeTargetDataFile(File file) {
        DataFileText targetTextFile = (DataFileText) dataFileText.cloneAll();
        targetTextFile.setFile(file);
        targetTextFile.setD2did(-1);
        targetTextFile.setCharset(writeOptionsController.charset);
        targetTextFile.setDelimiter(writeOptionsController.delimiterName);
        targetTextFile.setHasHeader(writeOptionsController.withNamesCheck.isSelected());
        return targetTextFile;
    }

}
