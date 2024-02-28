package mara.mybox.controller;

import java.nio.charset.Charset;
import java.util.HashMap;
import java.util.Map;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class DataFileCSVFormatController extends BaseChildController {

    protected BaseData2DLoadController fileController;

    @FXML
    protected ControlTextOptions optionsController;

    public void setParameters(BaseData2DLoadController parent) {
        try {
            fileController = parent;
            if (fileController == null || fileController.data2D == null) {
                close();
                return;
            }
            baseName = fileController.baseName;
            setFileType(fileController.TargetFileType);
            setTitle(message("Format") + " - " + fileController.getTitle());

            optionsController.setControls(baseName + "Read", true, false);
            optionsController.withNamesCheck.setSelected(fileController.data2D.isHasHeader());
            optionsController.setDelimiterName(fileController.data2D.getDelimiter());
            optionsController.setCharset(fileController.data2D.getCharset());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        Map<String, Object> options = new HashMap<>();
        Charset charset;
        if (optionsController.withNamesCheck.isSelected()) {
            charset = TextFileTools.charset(sourceFile);
        } else {
            charset = optionsController.getCharset();
        }
        options.put("hasHeader", optionsController.withNamesCheck.isSelected());
        options.put("charset", charset);
        options.put("delimiter", optionsController.getDelimiterValue());
        fileController.reloadFile(options);

        if (closeAfterCheck.isSelected()) {
            close();
        }
    }


    /*
        static methods
     */
    public static DataFileCSVFormatController open(BaseData2DLoadController parent) {
        try {
            if (parent == null) {
                return null;
            }
            DataFileCSVFormatController controller = (DataFileCSVFormatController) WindowTools.branchStage(
                    parent, Fxmls.DataFileCSVFormatFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
