package mara.mybox.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import mara.mybox.data.FileInformation;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.tools.ByteTools;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.logger;
import mara.mybox.value.CommonImageValues;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class FilesMergeController extends FilesBatchController {

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

    @FXML
    @Override
    public void selectTargetFile() {
        try {
            final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                    null, CommonImageValues.AllExtensionFilter, false);
            if (file == null) {
                return;
            }
            recordFileWritten(file);
            targetFileInput.setText(file.getAbsolutePath());
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @Override
    public void doCurrentProcess() {
        try {
            if (targetFile == null || tableData.isEmpty()) {
                return;
            }
            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;
            updateInterface("Started");
            synchronized (this) {
                if (task != null) {
                    return;
                }
                task = new SingletonTask<Void>() {

                    @Override
                    protected Void call() {
                        try {
                            int bufSize = 4096;
                            try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                                byte[] buf = new byte[bufSize];
                                int bufLen;
                                for (; currentParameters.currentIndex < sourcesIndice.size();) {
                                    if (isCancelled()) {
                                        break;
                                    }
                                    FileInformation d = tableData.get(sourcesIndice.get(currentParameters.currentIndex));
                                    try (FileInputStream inputStream
                                            = new FileInputStream(d.getFile())) {
                                        while ((bufLen = inputStream.read(buf)) != -1) {
                                            if (bufSize > bufLen) {
                                                buf = ByteTools.subBytes(buf, 0, bufLen);
                                            }
                                            outputStream.write(buf);
                                        }
                                    }
                                    d.setHandled(AppVariables.message("Successful"));
                                    tableView.refresh();
                                    currentParameters.currentIndex++;
                                    currentParameters.currentTotalHandled++;

                                    updateTaskProgress(currentParameters.currentIndex, sourcesIndice.size());

                                    if (isCancelled() || isPreview) {
                                        break;
                                    }
                                }
                            }
                            actualParameters.finalTargetName = targetFile.getAbsolutePath();
                            targetFiles.add(targetFile);
                            ok = true;
                        } catch (Exception e) {
                            logger.error(e.toString());
                        }

                        return null;
                    }

                    @Override
                    protected void succeeded() {
                        super.succeeded();
                        updateInterface("Done");

                    }

                    @Override
                    protected void cancelled() {
                        super.cancelled();
                        updateInterface("Canceled");
                    }

                    @Override
                    protected void failed() {
                        super.failed();
                        updateInterface("Failed");
                    }
                };
                operationBarController.progressValue.textProperty().bind(task.messageProperty());
                operationBarController.progressBar.progressProperty().bind(task.progressProperty());
                Thread thread = new Thread(task);
                thread.setDaemon(true);
                thread.start();
            }
        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }
    }

    @Override
    public void openTarget(ActionEvent event) {
        view(targetFile);
    }

}
