package mara.mybox.controller;

import java.io.File;
import java.util.List;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import mara.mybox.data.TTC;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.dev.MyBoxLog;
import mara.mybox.fxml.SingletonCurrentTask;
import mara.mybox.value.Languages;
import static mara.mybox.value.Languages.message;

/**
 * @Author Mara
 * @CreateDate 2020-12-01
 * @License Apache License Version 2.0
 */
public class FileTTC2TTFController extends HtmlTableController {

    protected TTC ttc;

    @FXML
    protected ControlFileSelecter ttcController;
    @FXML
    protected ControlPathInput targetPathInputController;

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
            ttcController.label(message("SourceFile"))
                    .isSource(true).isDirectory(false).mustExist(true).permitNull(false)
                    .type(VisitHistory.FileType.TTC)
                    .baseName(baseName).savedName(baseName + "TTC")
                    .init();

            targetPathInputController.mustExist(true).type(VisitHistory.FileType.TTF)
                    .baseName(baseName).init();

            startButton.disableProperty().unbind();
            startButton.disableProperty().bind(
                    ttcController.valid.not()
                            .or(targetPathInputController.valid.not())
            );

        } catch (Exception e) {
            MyBoxLog.error(e.toString());
        }
    }

    public void loadFile() {
        if (ttcController.file == null || !ttcController.file.exists() || !ttcController.file.isFile()) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

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

    @FXML
    @Override
    public void startAction() {
        targetPath = targetPathInputController.file;
        if (ttc == null || targetPath == null) {
            return;
        }
        if (task != null) {
            task.cancel();
        }
        task = new SingletonCurrentTask<Void>(this) {

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
