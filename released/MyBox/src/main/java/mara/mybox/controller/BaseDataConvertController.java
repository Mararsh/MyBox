package mara.mybox.controller;

import java.io.File;
import java.util.Date;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.tools.FileNameTools;
import mara.mybox.value.Languages;

/**
 * @Author Mara
 * @CreateDate 2020-12-05
 * @License Apache License Version 2.0
 */
public abstract class BaseDataConvertController extends BaseBatchFileController {

    @FXML
    protected VBox pdfOptionsVBox, convertVBox;
    @FXML
    protected ControlPdfWriteOptions pdfOptionsController;
    @FXML
    protected ControlDataConvert convertController;

    public BaseDataConvertController() {
        baseTitle = Languages.message("dataConvert");
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            pdfOptionsController.pixSizeRadio.setDisable(true);
            pdfOptionsController.standardSizeRadio.fire();

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (!convertController.initParameters()) {
            return false;
        }
        return super.makeMoreParameters();
    }

    public String filePrefix(File srcFile) {
        if (srcFile == null) {
            return null;
        }
        return FileNameTools.getFilePrefix(srcFile.getName()) + "_" + new Date().getTime();
    }

    @Override
    public void disableControls(boolean disable) {
        super.disableControls(disable);
        pdfOptionsVBox.setDisable(disable);
        convertVBox.setDisable(disable);
    }

}
