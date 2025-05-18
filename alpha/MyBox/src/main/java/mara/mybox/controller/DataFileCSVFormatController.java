package mara.mybox.controller;

import java.io.File;
import java.nio.charset.Charset;
import javafx.fxml.FXML;
import mara.mybox.data2d.DataFileCSV;
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
public class DataFileCSVFormatController extends BaseChildController {

    protected BaseData2DLoadController dataController;

    @FXML
    protected ControlTextOptions optionsController;

    public boolean isInvalid() {
        return dataController == null
                || !dataController.isShowing()
                || dataController.data2D == null
                || dataController.data2D.getFile() == null
                || !dataController.data2D.getFile().exists()
                || !(dataController.data2D instanceof DataFileCSV);
    }

    public void setParameters(BaseData2DLoadController parent) {
        try {
            dataController = parent;
            if (isInvalid()) {
                close();
                return;
            }
            baseName = dataController.baseName;
            setFileType(dataController.TargetFileType);
            setTitle(message("Format") + " - " + dataController.getTitle());

            optionsController.setControls(baseName + "Read", true, false);
            optionsController.withNamesCheck.setSelected(dataController.data2D.isHasHeader());
            optionsController.setDelimiterName(dataController.data2D.getDelimiter());
            optionsController.setCharset(dataController.data2D.getCharset());

        } catch (Exception e) {
            MyBoxLog.error(e);
        }
    }

    @FXML
    @Override
    public void okAction() {
        if (isInvalid()) {
            close();
            return;
        }
        File file = dataController.data2D.getFile();
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
                    if (optionsController.autoDetermine) {
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
                dataController.loadCSVFile(file, charset,
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
    public static DataFileCSVFormatController open(BaseData2DLoadController parent) {
        try {
            if (parent == null) {
                return null;
            }
            DataFileCSVFormatController controller = (DataFileCSVFormatController) WindowTools.referredTopStage(
                    parent, Fxmls.DataFileCSVFormatFxml);
            controller.setParameters(parent);
            return controller;
        } catch (Exception e) {
            MyBoxLog.error(e);
            return null;
        }
    }

}
