/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package mara.mybox.controller;

import java.io.File;
import java.util.Timer;
import java.util.TimerTask;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.scene.control.TextField;
import javafx.stage.Modality;
import mara.mybox.data.VisitHistory;
import mara.mybox.fxml.FxmlStage;
import mara.mybox.image.ImageFileInformation;
import mara.mybox.image.ImageInformation;
import mara.mybox.tools.FileTools;
import mara.mybox.value.AppVariables;
import mara.mybox.value.CommonFxValues;

/**
 * @Author Mara
 * @CreateDate 2018-6-21
 * @Description
 * @License Apache License Version 2.0
 */
public class ImageMetaDataController extends BaseController {

    @FXML
    private TextField fileInput;
    @FXML
    private TextArea metaDataInput;

    public ImageMetaDataController() {
        baseTitle = AppVariables.message("ImageMetaData");

        SourceFileType = VisitHistory.FileType.Text;
        SourcePathType = VisitHistory.FileType.Text;
        TargetPathType = VisitHistory.FileType.Text;
        TargetFileType = VisitHistory.FileType.Text;
        AddFileType = VisitHistory.FileType.Text;
        AddPathType = VisitHistory.FileType.Text;

        sourcePathKey = "TextFilePath";
        targetPathKey = "TextFilePath";

        sourceExtensionFilter = CommonFxValues.TextExtensionFilter;
        targetExtensionFilter = sourceExtensionFilter;

    }

    public void loadImageFileMeta(ImageInformation info) {
        fileInput.setText("");
        metaDataInput.setText("");
        if (info == null || info.getFileName() == null) {
            return;
        }
        fileInput.setText(info.getFileName());
        synchronized (this) {
            if (task != null) {
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
                                myStage.toFront();
                                metaDataInput.home();
                                metaDataInput.requestFocus();
                            });
                        }
                    }, 1000);
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
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
                null, targetExtensionFilter, true);
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
            if (task != null) {
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
                        FxmlStage.openTextEditer(null, file);
                    } else {
                        popSuccessful();
                    }
                }

            };
            openHandlingStage(task, Modality.WINDOW_MODAL);
            Thread thread = new Thread(task);
            thread.setDaemon(true);
            thread.start();
        }
    }

    @FXML
    public void editAction() {
        File file = FileTools.getTempFile(".txt");
        save(file, true);
    }

}
