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
import static mara.mybox.value.AppVariables.message;
import mara.mybox.value.CommonValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @License Apache License Version 2.0
 */
public class FilesMergeController extends BaseBatchFileController {

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

        startButton.disableProperty().unbind();
        startButton.disableProperty().bind(Bindings.isEmpty(targetFileInput.textProperty())
                .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(tableData))
        );

    }

    @Override
    public void selectTargetFileFromPath(File path) {
        try {
            final File file = chooseSaveFile(path, null, targetExtensionFilter);
            if (file == null) {
                return;
            }
            selectTargetFile(file);
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
        }
    }

    @Override
    public boolean makeMoreParameters() {
        if (!openWriter()) {
            return false;
        }
        return super.makeMoreParameters();
    }

    protected boolean openWriter() {
        try {
            outputStream = new BufferedOutputStream(new FileOutputStream(targetFile));
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

    @Override
    public String handleFile(File file) {
        try {
            if (!match(file)) {
                return message("Skip") + ": " + file;
            }
            byte[] buf = new byte[CommonValues.IOBufferLength];
            int bufLen;
            FileInformation d = (FileInformation) tableData.get(currentParameters.currentIndex);
            try ( BufferedInputStream inputStream = new BufferedInputStream(new FileInputStream(d.getFile()))) {
                while ((bufLen = inputStream.read(buf)) > 0) {
                    outputStream.write(buf, 0, bufLen);
                }
            }
            return message("Handled") + ": " + file;
        } catch (Exception e) {
            return file + " " + e.toString();
        }
    }

    protected boolean closeWriter() {
        try {
            outputStream.close();
            return true;
        } catch (Exception e) {
            updateLogs(e.toString(), true, true);
            return false;
        }
    }

    @Override
    public void donePost() {
        if (closeWriter()) {
            targetFileGenerated(targetFile);
        }
        super.donePost();
    }

}
