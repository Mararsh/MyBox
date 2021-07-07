package mara.mybox.controller;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import mara.mybox.db.data.VisitHistory;
import mara.mybox.fxml.FxmlWindow;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;

/**
 * @Author Mara
 * @CreateDate 2018-6-21
 * @License Apache License Version 2.0
 */
public class ImageMetaDataController extends BaseController {

    @FXML
    protected TextField fileInput;
    @FXML
    protected TextArea metaDataInput;

    public ImageMetaDataController() {
        baseTitle = AppVariables.message("ImageMetaData");
    }

    @Override
    public void setFileType() {
        setFileType(VisitHistory.FileType.Text);
    }

    public void loadImageFileMeta(ImageInformation info) {
        fileInput.setText("");
        metaDataInput.setText("");
        if (info == null || info.getFileName() == null) {
            return;
        }
        fileInput.setText(info.getFileName());
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                StringBuilder s;

                @Override
                protected boolean handle() {
                    ImageFileInformation finfo = info.getImageFileInformation();
                    s = new StringBuilder("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
                    s.append("<ImageMetadata file=\"").
                            append(finfo.getFileName()).
                            append("\"  numberOfImages=\"").
                            append(finfo.getNumberOfImages()).
                            append("\">\n");
                    int index = 1;
                    for (ImageInformation imageInfo : finfo.getImagesInformation()) {
                        s.append("    <Image index=\"").append(index).append("\">\n");
                        s.append(imageInfo.getMetaDataXml());
                        s.append("    </Image>\n");
                        index++;
                    }
                    s.append("</ImageMetadata>\n");
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    metaDataInput.setText(s.toString());
                    timer = new Timer();
                    timer.schedule(new TimerTask() {
                        @Override
                        public void run() {
                            Platform.runLater(() -> {
                                myStage.requestFocus();
                                metaDataInput.home();
                                metaDataInput.requestFocus();
                            });
                        }
                    }, 1000);
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
    public void saveAction() {
        saveAsAction();
    }

    @FXML
    @Override
    public void saveAsAction() {
        final File file = chooseSaveFile(AppVariables.getUserConfigPath(targetPathKey),
                null, targetExtensionFilter);
        if (file == null) {
            return;
        }
        recordFileWritten(file);
        save(file, false);
    }

    public void save(File file, boolean isEdit) {
        if (file == null) {
            return;
        }
        synchronized (this) {
            if (task != null && !task.isQuit()) {
                return;
            }
            task = new SingletonTask<Void>() {

                @Override
                protected boolean handle() {
                    ok = FileTools.writeFile(file, metaDataInput.getText()) != null;
                    return true;
                }

                @Override
                protected void whenSucceeded() {
                    if (isEdit) {
                        FxmlWindow.openTextEditer(null, file);
                    } else {
                        popSuccessful();
                    }
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
    public void editAction(ActionEvent event) {
        File file = FileTools.getTempFile(".txt");
        save(file, true);
    }

}
