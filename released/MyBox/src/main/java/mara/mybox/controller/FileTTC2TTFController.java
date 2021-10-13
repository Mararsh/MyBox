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
import mara.mybox.fxml.NodeStyleTools;
import mara.mybox.value.UserConfig;
import mara.mybox.value.AppVariables;
import static mara.mybox.value.Languages.message;
import mara.mybox.value.Languages;
import mara.mybox.value.UserConfig;
import thridparty.TTC;

/**
 * @Author Mara
 * @CreateDate 2020-12-01
 * @License Apache License Version 2.0
 */
public class FileTTC2TTFController extends HtmlTableController {

    protected TTC ttc;

    @FXML
    protected ControlFileSelecter ttcController;

    public FileTTC2TTFController() {
        baseTitle = Languages.message("TTC2TTF");
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
                    .label(Languages.message("SourceFile"))
                    .isSource(true).isDirectory(false).mustExist(true).permitNull(false)
                    .name(baseName + "TTC", true);

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    Bindings.isEmpty(ttcController.fileInput.textProperty())
                            .or(ttcController.fileInput.styleProperty().isEqualTo(UserConfig.badStyle()))
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
            start(task);
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
            File path = UserConfig.getPath(baseName + "TargetPath");
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
                    String info = Languages.message("ExtractedFiles") + ":";
                    for (File file : files) {
                        info += "\n    " + file.getAbsolutePath();
                    }
                    popInformation(info, 6000);
                }

            };
            start(task);
        }

    }

}
