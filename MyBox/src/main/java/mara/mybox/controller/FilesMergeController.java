package mara.mybox.controller;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import mara.mybox.data.FileInformation;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.value.CommonFxValues;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesMergeController extends FilesBatchController {

    protected BufferedOutputStream outputStream;

    public FilesMergeController() {
        baseTitle = AppVariables.message("FilesMerge");

    }

    @Override
    public void initTargetSection() {
        targetFileInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    targetFile = new File(newValue);
                    targetFileInput.setStyle(null);
                    recordFileWritten(targetFile.getParent());
                } catch (Exception e) {
                    targetFile = null;
                    targetFileInput.setStyle(badStyle);
                }
            }
        });

        operationBarController.openTargetButton.disableProperty().bind(Bindings.isEmpty(targetFileInput.textProperty())
                .or(targetFileInput.styleProperty().isEqualTo(badStyle))
        );

        startButton.disableProperty().bind(Bindings.isEmpty(targetFileInput.textProperty())
                .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(tableData))
        );

    }

    @Override
    public void selectTargetFileFromPath(File path) {
        try {
            final File file = chooseSaveFile(path, null, CommonFxValues.AllExtensionFilter, true);
            if (file == null) {
                return;
            }
            selectTargetFile(file);
        } catch (Exception e) {
//            MyBoxLog.error(e.toString());
        }
    }

    @Override
    public boolean makeMoreParameters() {
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
        } catch (Exception e) {
            return false;
        }
        return super.makeMoreParameters();
    }

    @Override
    public String handleFile(File file) {
        try {
            countHandling(file);
            if (!match(file)) {
                return AppVariables.message("Skip");
            }
            byte[] buf = new byte[CommonValues.IOBufferLength];
            int bufLen;
            FileInformation d = (FileInformation) tableData.get(currentParameters.currentIndex);
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(d.getFile()))) {
                while ((bufLen = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bufLen);
                }
            }
            return AppVariables.message("Successful");
        } catch (Exception e) {
            return AppVariables.message("Failed");
        }
    }

    @Override
    public void donePost() {
        try {
            outputStream.close();
            targetFileGenerated(targetFile);
        } catch (Exception e) {
            MyBoxLog.debug(e.toString());
        }
        super.donePost();
    }

}
