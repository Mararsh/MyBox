package mara.mybox.controller;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.util.Date;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.concurrent.Task;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.stage.FileChooser;
import static mara.mybox.fxml.FxmlTools.badStyle;
import mara.mybox.objects.AppVaribles;
import static mara.mybox.objects.AppVaribles.logger;
import mara.mybox.objects.CommonValues;
import mara.mybox.objects.FileInformation;
import mara.mybox.tools.ByteTools;

/**
 * @Author Mara
 * @CreateDate 2018-9-21
 * @Description
 * @License Apache License Version 2.0
 */
public class FileMergeController extends FilesBatchController {

    private File targetFile;

    @Override
    protected void initTargetSection() {
        targetFileInput.textProperty().addListener(new ChangeListener<String>() {
            @Override
            public void changed(ObservableValue<? extends String> observable, String oldValue, String newValue) {
                try {
                    targetFile = new File(newValue);
                    targetFileInput.setStyle(null);
                    AppVaribles.setUserConfigValue(LastPathKey, targetFile.getParent());
                    AppVaribles.setUserConfigValue(targetPathKey, targetFile.getParent());
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
    protected void selectTargetFile(ActionEvent event) {
        try {
            final FileChooser fileChooser = new FileChooser();
            File path = new File(AppVaribles.getUserConfigPath(targetPathKey, CommonValues.UserFilePath));
            fileChooser.setInitialDirectory(path);
            fileChooser.getExtensionFilters().addAll(CommonValues.PdfExtensionFilter);
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
    protected void doCurrentProcess() {
        try {
            if (targetFile == null || sourceFilesInformation.isEmpty()) {
                return;
            }
            currentParameters.startTime = new Date();
            currentParameters.currentTotalHandled = 0;
            updateInterface("Started");
            task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        int bufSize = 4096;
                        try (FileOutputStream outputStream = new FileOutputStream(targetFile)) {
                            byte[] buf = new byte[bufSize];
                            int bufLen;
                            for (; currentParameters.currentIndex < sourcesHandling.size();) {
                                if (isCancelled()) {
                                    break;
                                }
                                FileInformation d = sourceFilesInformation.get(sourcesHandling.get(currentParameters.currentIndex));
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
                                sourceTableView.refresh();
                                currentParameters.currentIndex++;
                                currentParameters.currentTotalHandled++;
                                updateProgress(currentParameters.currentIndex, sourcesHandling.size());
                                updateMessage(currentParameters.currentIndex + "/" + sourcesHandling.size());

                                if (isCancelled() || isPreview) {
                                    break;
                                }
                            }
                        }
                    } catch (Exception e) {
                        logger.error(e.toString());
                    }

                    if (!isPreview) {
                        Platform.runLater(new Runnable() {
                            @Override
                            public void run() {
                                openTarget(null);
                            }
                        });
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
    protected void viewFile(File file) {
        if (file == null) {
            return;
        }
        if (file.isFile()) {
            BytesEditerController controller = (BytesEditerController) openStage(CommonValues.BytesEditerFxml,
                    AppVaribles.getMessage("BytesEditer"), false, true);
            controller.openFile(file);
        } else {
            OpenFile.openTarget(getClass(), null, file.getAbsolutePath());
        }
    }

    @Override
    protected void openTarget(ActionEvent event) {
        OpenFile.openTarget(getClass(), null, targetFile.getAbsolutePath());
    }

}
