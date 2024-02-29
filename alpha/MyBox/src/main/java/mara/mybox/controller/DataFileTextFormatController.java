package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.FxSingletonTask;
import mara.mybox.fxml.WindowTools;
import mara.mybox.tools.TextFileTools;
import mara.mybox.value.Fxmls;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2023-7-19
 * @License Apache License Version 2.0
 */
public class DataFileTextFormatController extends BaseChildController {

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

            optionsController.setControls(baseName + "Read", true, true);
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
        if (fileController == null || !fileController.isShowing()
                || fileController.data2D == null
                || fileController.data2D.getFile() == null) {
            close();
            return;
        }
        File file = fileController.data2D.getFile();
        if (file == null || !file.exists()) {
            close();
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new FxSingletonTask<Void>(this) {
            Charset charset;

            @Override
            protected boolean handle() {
                try {
                    if (optionsController.withNamesCheck.isSelected()) {
                        charset = TextFileTools.charset(file);
                    } else {
                        charset = optionsController.getCharset();
                    }
                    return true;
                } catch (Exception e) {
                    error = e.toString();
                    return false;
                }
            }

            @Override
            protected void whenSucceeded() {
                fileController.loadTextFile(file, charset,
                        optionsController.withNamesCheck.isSelected(),
                        optionsController.getDelimiterValue());
                if (closeAfterCheck.isSelected()) {
                    close();
                }
            }

        };
        start(task);

    }


    /*
        static methods
     */
    public static DataFileTextFormatController open(BaseData2DLoadController parent) {
        try {
            if (parent == null) {
                return null;
            }
            DataFileTextFormatController controller = (DataFileTextFormatController) WindowTools.branchStage(
                    parent, Fxmls.DataFileTextFormatFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
