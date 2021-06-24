package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.binding.Bindings;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.stage.DirectoryChooser;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import static mara.mybox.fxml.FxmlControl.badStyle;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.AppVariables.message;
import thridparty.TTC;

/**
 * @Author Mara
 * @CreateDate 2020-12-01
 * @License Apache License Version 2.0
 */
public class FileTTC2TTFController extends HtmlViewerController {

    protected TTC ttc;

    @FXML
    protected ControlFileSelecter ttcController;

    public FileTTC2TTFController() {
        baseTitle = AppVariables.message("TTC2TTF");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.TTC, VisitHistory.FileType.TTF);
    }

    @Override
    public void initControls() {
        try {
            super.initControls();

            ttcController.notify.addListener(
                    (ObservableValue<? extends Boolean> observable, Boolean oldValue, Boolean newValue) -> {
                        loadFile();
                    });

            ttcController.type(VisitHistory.FileType.TTC)
                    .label(message("SourceFile"))
                    .isSource(true).isDirectory(false).mustExist(true).permitNull(false)
                    .name(baseName + "TTC", true);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(ttcController.fileInput.textProperty())
                            .or(ttcController.fileInput.styleProperty().isEqualTo(badStyle))
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadFile() {
        if (ttcController.file == null || !ttcController.file.exists() || !ttcController.file.isFile()) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    try {
                        ttc = new TTC(ttcController.file);
                        ttc.parseFile();
                        return true;
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    loadBody(ttc.html());
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }
    }

    @FXML
    @Override
    public void startAction() {
        if (ttc == null) {
            return;
        }
        try {
            DirectoryChooser chooser = new DirectoryChooser();
            File path = AppVariables.getUserConfigPath(targetPathKey);
            if (path != null) {
                chooser.setInitialDirectory(path);
            }
            targetPath = chooser.showDialog(getMyStage());
            if (targetPath == null) {
                return;
            }
            selectTargetPath(targetPath);
        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                private List<File> files;

                @Override
                protected boolean handle() {
                    try {
                        if (ttc.getTtfInfos() == null) {
                            ttc.parseFile();
                        }
                        if (ttc.getTtfInfos() == null) {
                            return false;
                        }
                        files = ttc.extract(targetPath);
                        return files != null && !files.isEmpty();
                    } catch (Exception e) {
                        error = e.toString();
                        return false;
                    }
                }

                @Override
                protected void whenSucceeded() {
                    browseURI(targetPath.toURI());
                    String info = message("ExtractedFiles") + ":";
                    for (File file : files) {
                        info += "\n    " + file.getAbsolutePath();
                    }
                    popInformation(info, 6000);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            task.setSelf(task);
            Thread thread = new Thread(task);
            thread.setDaemon(false);
            thread.start();
        }

    }

}
