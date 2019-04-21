package mara.mybox.controller;

import mara.mybox.controller.base.BatchBaseController;
import mara.mybox.fxml.FxmlStage;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVaribles;
import static mara.mybox.value.AppVaribles.logger;
import mara.mybox.value.CommonValues;
import mara.mybox.data.FileInformation;
import mara.mybox.tools.ByteTools;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class FileMergeController extends BatchBaseController {

    public FileMergeController() {
        baseTitle = AppVaribles.getMessage("FileMerge");

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

        operationBarController.startButton.disableProperty().bind(Bindings.isEmpty(targetFileInput.textProperty())
                .or(targetFileInput.styleProperty().isEqualTo(badStyle))
                .or(Bindings.isEmpty(sourceFilesInformation))
        );

    }

    @FXML
    @Override
    public void selectTargetFile(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = AppVaribles.getUserConfigPath(targetPathKey);
            if (path.exists()) {
                fileChooser.setInitialDirectory(path);
            }
            fileChooser.getExtensionFilters().addAll(CommonValues.AllExtensionFilter);
            final File file = fileChooser.showSaveDialog(getMyStage());
            if (file == null) {
                return;
            }
            targetFileInput.setText(file.getAbsolutePath());
        } catch (Exception e) {
//            logger.error(e.toString());
        }
    }

    @Override
    public void makeMoreParameters() {
        makeBatchParameters();
    }

    @Override
    public void doCurrentProcess() {
        try {
            if (targetFile == null || sourceFilesInformation.isEmpty()) {
                return;
            }
            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;
            updateInterface("Started");
            task = new Task<Void>() {
                private boolean ok;

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
                                FileInformation d = sourceFilesInformation.get(sourcesIndice.get(currentParameters.currentIndex));
                                try (FileInputStream inputStream
                                        = new FileInputStream(d.getFile())) {
                                    while ((bufLen = inputStream.read(buf)) != -1) {
                                        if (bufSize > bufLen) {
                                            buf = ByteTools.subBytes(buf, 0, bufLen);
                                        }
                                        outputStream.write(buf);
                                    }
                                }
                                d.setHandled(AppVaribles.getMessage("Successful"));
                                filesTableView.refresh();
                                currentParameters.currentIndex++;
                                currentParameters.currentTotalHandled++;
                                updateProgress(currentParameters.currentIndex, sourcesIndice.size());
                                updateMessage(currentParameters.currentIndex + "/" + sourcesIndice.size());

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

        } catch (Exception e) {
            updateInterface("Failed");
            logger.error(e.toString());
        }
    }

    @Override
    public void openTarget(ActionEvent event) {
        FxmlStage.openTarget(getClass(), null, targetFile.getAbsolutePath());
    }

}
